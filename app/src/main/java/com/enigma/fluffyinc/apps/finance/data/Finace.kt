package com.enigma.fluffyinc.apps.finance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

//Income.kt
/**
 * Represents an income with its details.
 * @property id The unique identifier for the income.
 * @property amount The income amount.
 * @property description A description or reason for the income.
 * @property source The source of the income (e.g., salary, bonus, investment).
 * @property category The category of the income (e.g., salary, bonus, investment).
 * @property date The date when the income occurred.
 */


@Entity(tableName = "income")
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val source: String,
    val category: String, // family and friends, business, salary, donations, other
    val date: Long = System.currentTimeMillis()
)
//Expense.kt
/**
 * Represents an expense with its details.
 * @property id The unique identifier for the expense.
 * @property amount The amount spent.
 * @property description A description or reason for the expense.
 * @property source The source of the expense (e.g., bank transfer, credit card).
 * @property category The category of the expense (e.g., shopping, scheduled payments, leisure, rent, food, other).
 * @property date The date when the expense occurred.
 * @property shoppingListId Link to shopping list if from shopping.
 */


@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val source: String,
    val category: String, // shopping, scheduled payments, leisure, rent, food, other
    val date: Long = System.currentTimeMillis(),
    val shoppingListId: Long? = null // Link to shopping list if from shopping
)
//Scheduled Payment
/**
 * Represents a scheduled payment with its details.
 * @property id The unique identifier for the scheduled payment.
 * @property amount The amount to be paid.
 * @property description A description or reason for the payment.
 * @property source The source of the payment (e.g., bank transfer, credit card).
 * @property frequency The frequency of the payment (e.g., weekly, monthly, annually).
 * @property nextPaymentDate The date when the next payment should be made.
 * @property numberOfPayments The total number of payments (null means indefinite).
 * @property paymentsMade The number of payments made so far.
 * @property isActive Whether the payment is currently active or not.
 */


@Entity(tableName = "scheduled_payment")
data class ScheduledPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val source: String,
    val frequency: String, // weekly, monthly, annually
    val nextPaymentDate: Long,
    val numberOfPayments: Int?, // null means indefinite
    val paymentsMade: Int = 0,
    val isActive: Boolean = true
)

// ShoppingList.kt
/**
 * Represents a shopping list with items.
 * @property id The unique identifier for the shopping list.
 * @property name The name of the shopping list.
 * @property isCompleted Whether the shopping list is completed or not.
 * @property completedDate The date when the shopping list was completed (if applicable).
 * @property totalAmount The total amount of items in the shopping list.
 */

@Entity(tableName = "shopping_list")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null,
    val totalAmount: Double? = null
)

// ShoppingItem.kt
/**
 * Represents an item in a shopping list.
 * @property id The unique identifier for the shopping item.
 * @property shoppingListId The identifier of the shopping list this item belongs to.
 * @property itemName The name of the item.
 * @property quantity The quantity of the item.
 * @property pricePerItem The price per unit of the item.
 * @property shoppingType The type of shopping item (e.g., groceries, electronics).
 * @property isChecked Whether the item has been checked or not.
 */
@Entity(tableName = "shopping_item")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shoppingListId: Long,
    val itemName: String,
    val quantity: Int,
    val pricePerItem: Double,
    val shoppingType: String,
    val isChecked: Boolean = false
)

@Entity(tableName = "loan")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanName: String,
    val principalAmount: Double,
    val interestRate: Double, // Annual interest rate in percentage
    val loanTerm: Int, // Number of repayment periods
    val repaymentFrequency: String, // weekly, monthly, annually
    val startDate: Long,
    val nextPaymentDate: Long,
    val totalInterest: Double, // Total interest to be paid
    val totalAmount: Double, // Principal + Interest
    val amountPaid: Double = 0.0,
    val numberOfPaymentsMade: Int = 0,
    val isActive: Boolean = true,
    val loanType: String ,// personal, business, mortgage, other
    val monthlyPayment: Double = totalAmount/loanTerm
)

@Entity(tableName = "loan_payment")
data class LoanPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val amount: Double,
    val principalPortion: Double,
    val interestPortion: Double,
    val date: Long,
    val paymentNumber: Int
)