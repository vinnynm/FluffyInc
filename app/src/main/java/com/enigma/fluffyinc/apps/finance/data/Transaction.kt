package com.enigma.fluffyinc.apps.finance.data

sealed class Transaction {
    abstract val date: Long
    abstract val amount: Double
    abstract val description: String

    data class IncomeTransaction(
        override val date: Long,
        override val amount: Double,
        override val description: String,
        val income: Income
    ) : Transaction()

    data class ExpenseTransaction(
        override val date: Long,
        override val amount: Double,
        override val description: String,
        val expense: Expense
    ) : Transaction()
}