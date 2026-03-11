package com.enigma.fluffyinc.apps.finance.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        Income::class,
        Expense::class,
        ScheduledPayment::class,
        ShoppingList::class,
        ShoppingItem::class,
        Loan::class,
        LoanPayment::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun scheduledPaymentDao(): ScheduledPaymentDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun loanDao(): LoanDao

    abstract fun loanPaymentDao(): LoanPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
