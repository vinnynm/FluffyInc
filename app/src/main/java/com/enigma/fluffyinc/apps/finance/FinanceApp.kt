package com.enigma.fluffyinc.apps.finance

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.enigma.fluffyinc.apps.finance.screens.DashboardScreen
import com.enigma.fluffyinc.apps.finance.screens.ExpensesScreen
import com.enigma.fluffyinc.apps.finance.screens.FinanceViewModel
import com.enigma.fluffyinc.apps.finance.screens.IncomeScreen
import com.enigma.fluffyinc.apps.finance.screens.LoanDetailScreen
import com.enigma.fluffyinc.apps.finance.screens.LoansScreen
import com.enigma.fluffyinc.apps.finance.screens.ScheduledPaymentsScreen
import com.enigma.fluffyinc.apps.finance.screens.ShoppingListDetailScreen
import com.enigma.fluffyinc.apps.finance.screens.ShoppingListsScreen
import com.enigma.fluffyinc.apps.finance.screens.TransactionsScreen
import com.enigma.fluffyinc.apps.finance.screens.ShoppingStatsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(viewModel: FinanceViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") { DashboardScreen(viewModel, navController) }
        composable("income") { IncomeScreen(viewModel, navController) }
        composable("expenses") { ExpensesScreen(viewModel, navController) }
        composable("scheduled") { ScheduledPaymentsScreen(viewModel, navController) }
        composable("shopping") { ShoppingListsScreen(viewModel, navController) }
        composable("shopping/{listId}") { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")?.toLongOrNull()
            if (listId != null) {
                ShoppingListDetailScreen(viewModel, listId, navController)
            }
        }
        composable("shopping_stats") { ShoppingStatsScreen(viewModel, navController) }
        composable("transactions") { TransactionsScreen(viewModel, navController) }
        composable("loans") { LoansScreen(viewModel, navController) }
        composable("loan/{loanId}") { backStackEntry ->
            val loanId = backStackEntry.arguments?.getString("loanId")?.toLongOrNull()
            if (loanId != null) {
                LoanDetailScreen(viewModel, loanId, navController)
            }
        }
    }
}
