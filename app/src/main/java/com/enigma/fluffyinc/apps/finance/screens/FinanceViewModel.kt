package com.enigma.fluffyinc.apps.finance.screens


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.apps.finance.data.Expense
import com.enigma.fluffyinc.apps.finance.data.FinanceDatabase
import com.enigma.fluffyinc.apps.finance.data.Income
import com.enigma.fluffyinc.apps.finance.data.Loan
import com.enigma.fluffyinc.apps.finance.data.LoanPayment
import com.enigma.fluffyinc.apps.finance.data.ScheduledPayment
import com.enigma.fluffyinc.apps.finance.data.ShoppingItem
import com.enigma.fluffyinc.apps.finance.data.ShoppingList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar


class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FinanceDatabase.getDatabase(application)
    private val incomeDao = database.incomeDao()
    private val expenseDao = database.expenseDao()
    private val scheduledPaymentDao = database.scheduledPaymentDao()
    private val shoppingListDao = database.shoppingListDao()
    private val shoppingItemDao = database.shoppingItemDao()
    private val  loanPaymentDao = database.loanPaymentDao()
    private val loanDao = database.loanDao()

    val allLoans = loanDao.getAllLoans().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeLoans = loanDao.getActiveLoans().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allIncome = incomeDao.getAllIncome().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allExpenses = expenseDao.getAllExpenses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activePayments = scheduledPaymentDao.getActivePayments().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeShoppingLists = shoppingListDao.getActiveShoppingLists().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val completedShoppingLists = shoppingListDao.getCompletedShoppingLists().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val shoppingTypes = shoppingItemDao.getAllShoppingTypes().stateIn(viewModelScope, SharingStarted.Lazily, listOf("Groceries", "Electronics", "Clothing", "Other"))

    init {
        viewModelScope.launch {
            processScheduledPayments()
        }
    }

    // Income operations
    fun addIncome(income: Income) = viewModelScope.launch {
        incomeDao.insert(income)
    }

    fun deleteIncome(income: Income) = viewModelScope.launch {
        incomeDao.delete(income)
    }

    // Expense operations
    fun addExpense(expense: Expense) = viewModelScope.launch {
        expenseDao.insert(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseDao.delete(expense)
    }

    // Scheduled payment operations
    fun addScheduledPayment(payment: ScheduledPayment) = viewModelScope.launch {
        scheduledPaymentDao.insert(payment)
    }

    fun updateScheduledPayment(payment: ScheduledPayment) = viewModelScope.launch {
        scheduledPaymentDao.update(payment)
    }

    fun cancelScheduledPayment(payment: ScheduledPayment) = viewModelScope.launch {
        scheduledPaymentDao.update(payment.copy(isActive = false))
    }

    private suspend fun processScheduledPayments() {
        val currentTime = System.currentTimeMillis()
        val duePayments = scheduledPaymentDao.getDuePayments(currentTime)

        duePayments.forEach { payment ->
            // Add expense
            addExpense(Expense(
                amount = payment.amount,
                description = payment.description,
                source = payment.source,
                category = "scheduled payments",
                date = currentTime
            ))

            // Update payment
            val newPaymentsMade = payment.paymentsMade + 1
            val shouldDeactivate = payment.numberOfPayments?.let { newPaymentsMade >= it } ?: false

            if (shouldDeactivate) {
                scheduledPaymentDao.update(payment.copy(
                    paymentsMade = newPaymentsMade,
                    isActive = false
                ))
            } else {
                val nextDate = calculateNextPaymentDate(payment.nextPaymentDate, payment.frequency)
                scheduledPaymentDao.update(payment.copy(
                    paymentsMade = newPaymentsMade,
                    nextPaymentDate = nextDate
                ))
            }
        }
    }

    private fun calculateNextPaymentDate(currentDate: Long, frequency: String): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate

        when (frequency.lowercase()) {
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "annually" -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis
    }

    // Shopping list operations
    fun createShoppingList(name: String) = viewModelScope.launch {
        shoppingListDao.insert(ShoppingList(name = name))
    }

    fun addShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        shoppingItemDao.insert(item)
    }

    fun updateShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        shoppingItemDao.update(item)
    }

    fun deleteShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        shoppingItemDao.delete(item)
    }

    fun getShoppingItems(listId: Long) = shoppingItemDao.getItemsByListId(listId)

    fun completeShoppingList(listId: Long) = viewModelScope.launch {
        val shoppingList = shoppingListDao.getShoppingListById(listId) ?: return@launch
        val items = shoppingItemDao.getItemsByListId(listId).first()

        val checkedItems = items.filter { it.isChecked }
        val uncheckedItems = items.filter { !it.isChecked }

        // Calculate total
        val total = checkedItems.sumOf { it.quantity * it.pricePerItem }

        // Mark original list as completed (keeps checked items)
        shoppingListDao.update(shoppingList.copy(
            isCompleted = true,
            completedDate = System.currentTimeMillis(),
            totalAmount = total
        ))

        // Add expense
        addExpense(Expense(
            amount = total,
            description = "Shopping: ${shoppingList.name}",
            source = shoppingList.name,
            category = "shopping",
            date = System.currentTimeMillis(),
            shoppingListId = listId
        ))

        // Create new list with unchecked items if any
        if (uncheckedItems.isNotEmpty()) {
            val newListId = shoppingListDao.insert(ShoppingList(name = "${shoppingList.name} (Remaining)"))
            uncheckedItems.forEach { item ->
                shoppingItemDao.insert(item.copy(id = 0, shoppingListId = newListId, isChecked = false))
            }
            // Remove unchecked items from completed list
            uncheckedItems.forEach { item ->
                shoppingItemDao.delete(item)
            }
        }
    }

    // Analytics
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<Map<String, Double>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
            .map { expenses ->
                expenses.groupBy { it.category }
                    .mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }
            }
    }

    fun getShoppingExpensesByType(startDate: Long, endDate: Long): Flow<Map<String, Double>> {
        return flow {
            val expenses = expenseDao.getShoppingExpensesByDateRange(startDate, endDate).first()
            val typeMap = mutableMapOf<String, Double>()

            expenses.forEach { expense ->
                expense.shoppingListId?.let { listId ->
                    val items = shoppingItemDao.getItemsByListId(listId).first()
                    items.filter { it.isChecked }.forEach { item ->
                        val amount = item.quantity * item.pricePerItem
                        typeMap[item.shoppingType] = typeMap.getOrDefault(item.shoppingType, 0.0) + amount
                    }
                }
            }

            emit(typeMap)
        }
    }


    // ============================================
    // LOAN OPERATIONS
    // ============================================

    fun addLoan(
        loanName: String,
        principalAmount: Double,
        interestRate: Double,
        loanTerm: Int,
        repaymentFrequency: String,
        loanType: String
    ) = viewModelScope.launch {
        val startDate = System.currentTimeMillis()

        // Calculate payment details based on frequency
        val periodsPerYear = when (repaymentFrequency.lowercase()) {
            "weekly" -> 52
            "monthly" -> 12
            "annually" -> 1
            else -> 12
        }

        val periodicRate = interestRate / 100 / periodsPerYear
        val totalPayments = loanTerm

        // Calculate payment amount using amortization formula
        // Payment = P * [r(1+r)^n] / [(1+r)^n - 1]
        val monthlyPayment = if (periodicRate > 0) {
            principalAmount * (periodicRate * Math.pow(1 + periodicRate, totalPayments.toDouble())) /
                    (Math.pow(1 + periodicRate, totalPayments.toDouble()) - 1)
        } else {
            principalAmount / totalPayments
        }

        val totalAmount = monthlyPayment * totalPayments
        val totalInterest = totalAmount - principalAmount

        // Calculate next payment date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        when (repaymentFrequency.lowercase()) {
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "annually" -> calendar.add(Calendar.YEAR, 1)
        }

        val loan = Loan(
            loanName = loanName,
            principalAmount = principalAmount,
            interestRate = interestRate,
            loanTerm = loanTerm,
            repaymentFrequency = repaymentFrequency,
            startDate = startDate,
            nextPaymentDate = calendar.timeInMillis,
            monthlyPayment = monthlyPayment,
            totalInterest = totalInterest,
            totalAmount = totalAmount,
            loanType = loanType
        )

        loanDao.insert(loan)

        // Add loan amount to income
        addIncome(Income(
            amount = principalAmount,
            description = "Loan: $loanName",
            source = loanType,
            category = "Other"
        ))
    }

    fun makeLoanPayment(loanId: Long) = viewModelScope.launch {
        val loan = loanDao.getLoanById(loanId) ?: return@launch

        val paymentNumber = loan.numberOfPaymentsMade + 1
        val remainingPrincipal = loan.principalAmount - loan.amountPaid

        // Calculate interest and principal portions
        val periodsPerYear = when (loan.repaymentFrequency.lowercase()) {
            "weekly" -> 52
            "monthly" -> 12
            "annually" -> 1
            else -> 12
        }
        val periodicRate = loan.interestRate / 100 / periodsPerYear
        val interestPortion = remainingPrincipal * periodicRate
        val principalPortion = loan.monthlyPayment - interestPortion

        // Record payment
        val payment = LoanPayment(
            loanId = loanId,
            amount = loan.monthlyPayment,
            principalPortion = principalPortion,
            interestPortion = interestPortion,
            date = System.currentTimeMillis(),
            paymentNumber = paymentNumber
        )
        loanPaymentDao.insert(payment)

        // Update loan
        val newAmountPaid = loan.amountPaid + principalPortion
        val isComplete = paymentNumber >= loan.loanTerm

        if (isComplete) {
            loanDao.update(loan.copy(
                amountPaid = loan.principalAmount,
                numberOfPaymentsMade = paymentNumber,
                isActive = false
            ))
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = loan.nextPaymentDate
            when (loan.repaymentFrequency.lowercase()) {
                "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "monthly" -> calendar.add(Calendar.MONTH, 1)
                "annually" -> calendar.add(Calendar.YEAR, 1)
            }

            loanDao.update(loan.copy(
                amountPaid = newAmountPaid,
                numberOfPaymentsMade = paymentNumber,
                nextPaymentDate = calendar.timeInMillis
            ))
        }

        // Add to expenses
        addExpense(Expense(
            amount = loan.monthlyPayment,
            description = "Loan Payment: ${loan.loanName}",
            source = loan.loanType,
            category = "other"
        ))
    }

    fun payOffLoan(loanId: Long) = viewModelScope.launch {
        val loan = loanDao.getLoanById(loanId) ?: return@launch
        val remainingAmount = loan.principalAmount - loan.amountPaid

        if (remainingAmount > 0) {
            // Record final payment
            val payment = LoanPayment(
                loanId = loanId,
                amount = remainingAmount,
                principalPortion = remainingAmount,
                interestPortion = 0.0,
                date = System.currentTimeMillis(),
                paymentNumber = loan.numberOfPaymentsMade + 1
            )
            loanPaymentDao.insert(payment)

            // Mark loan as complete
            loanDao.update(loan.copy(
                amountPaid = loan.principalAmount,
                numberOfPaymentsMade = loan.numberOfPaymentsMade + 1,
                isActive = false
            ))

            // Add to expenses
            addExpense(Expense(
                amount = remainingAmount,
                description = "Loan Payoff: ${loan.loanName}",
                source = loan.loanType,
                category = "other"
            ))
        }
    }

    fun deleteLoan(loan: Loan) = viewModelScope.launch {
        loanDao.delete(loan)
    }

    fun getLoanPayments(loanId: Long) = loanPaymentDao.getPaymentsByLoanId(loanId)

    private suspend fun processLoanPayments() {
        val currentTime = System.currentTimeMillis()
        val dueLoans = loanDao.getDueLoans(currentTime)

        dueLoans.forEach { loan ->
            makeLoanPayment(loan.id)
        }
    }
}
