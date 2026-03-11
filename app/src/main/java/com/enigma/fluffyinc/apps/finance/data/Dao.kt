package com.enigma.fluffyinc.apps.finance.data


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insert(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<Income>>

    @Query("SELECT * FROM income WHERE date BETWEEN :startDate AND :endDate")
    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<Income>>
}

// ExpenseDao.kt


@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expense ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE date BETWEEN :startDate AND :endDate")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE category = 'shopping' AND date BETWEEN :startDate AND :endDate")
    fun getShoppingExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE category = 'food' AND date BETWEEN :startDate AND :endDate")
    fun getFoodExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

}

// ScheduledPaymentDao.kt


@Dao
interface ScheduledPaymentDao {
    @Insert
    suspend fun insert(payment: ScheduledPayment)

    @Update
    suspend fun update(payment: ScheduledPayment)

    @Delete
    suspend fun delete(payment: ScheduledPayment)

    @Query("SELECT * FROM scheduled_payment WHERE isActive = 1 ORDER BY nextPaymentDate ASC")
    fun getActivePayments(): Flow<List<ScheduledPayment>>

    @Query("SELECT * FROM scheduled_payment WHERE isActive = 1 AND nextPaymentDate <= :currentDate")
    suspend fun getDuePayments(currentDate: Long): List<ScheduledPayment>
}

// ShoppingListDao.kt


@Dao
interface ShoppingListDao {
    @Insert
    suspend fun insert(shoppingList: ShoppingList): Long

    @Update
    suspend fun update(shoppingList: ShoppingList)

    @Delete
    suspend fun delete(shoppingList: ShoppingList)

    @Query("SELECT * FROM shopping_list WHERE isCompleted = 0 ORDER BY id DESC")
    fun getActiveShoppingLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_list WHERE isCompleted = 1 ORDER BY completedDate DESC")
    fun getCompletedShoppingLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_list WHERE id = :id")
    suspend fun getShoppingListById(id: Long): ShoppingList?
}

// ShoppingItemDao.kt


@Dao
interface ShoppingItemDao {
    @Insert
    suspend fun insert(item: ShoppingItem)

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("SELECT * FROM shopping_item WHERE shoppingListId = :listId")
    fun getItemsByListId(listId: Long): Flow<List<ShoppingItem>>

    @Query("SELECT DISTINCT shoppingType FROM shopping_item")
    fun getAllShoppingTypes(): Flow<List<String>>
}



@Dao
interface LoanDao {
    @Insert
    suspend fun insert(loan: Loan): Long

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    @Query("SELECT * FROM loan WHERE isActive = 1 ORDER BY nextPaymentDate ASC")
    fun getActiveLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loan ORDER BY startDate DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loan WHERE id = :id")
    suspend fun getLoanById(id: Long): Loan?

    @Query("SELECT * FROM loan WHERE isActive = 1 AND nextPaymentDate <= :currentDate")
    suspend fun getDueLoans(currentDate: Long): List<Loan>
}


@Dao
interface LoanPaymentDao {
    @Insert
    suspend fun insert(payment: LoanPayment)

    @Update
    suspend fun update(payment: LoanPayment)

    @Delete
    suspend fun delete(payment: LoanPayment)

    @Query("SELECT * FROM loan_payment WHERE loanId = :loanId ORDER BY date DESC")
    fun getPaymentsByLoanId(loanId: Long): Flow<List<LoanPayment>>

    @Query("SELECT * FROM loan_payment ORDER BY date DESC")
    fun getAllPayments(): Flow<List<LoanPayment>>
}