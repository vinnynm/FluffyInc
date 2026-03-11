package com.enigma.fluffyinc.apps.finance.screens


import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.ui.theme.FluffyIncTheme
import java.text.NumberFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: FinanceViewModel, navController: NavController) {
    val income by viewModel.allIncome.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    val totalIncome = income.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val balance = totalIncome - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Manager") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= 0)  MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (balance >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        NumberFormat.getCurrencyInstance().format(balance),
                        style = MaterialTheme.typography.displayMedium,
                        color = if (balance >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Income",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (balance >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                NumberFormat.getCurrencyInstance().format(totalIncome),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (balance >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                NumberFormat.getCurrencyInstance().format(totalExpenses),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }

            // Navigation Grid
            Text(
                "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Income",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = Color(0xFF4CAF50),
                        onClick = { navController.navigate("income") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Expenses",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = Color(0xFFF44336),
                        onClick = { navController.navigate("expenses") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Scheduled Payments",
                        icon = Icons.Default.DateRange,
                        color = Color(0xFFFF9800),
                        onClick = { navController.navigate("scheduled") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Shopping Lists",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF2196F3),
                        onClick = { navController.navigate("shopping") }
                    )
                }
                item {
                    DashboardCard(
                        title = "All Transactions",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFF9C27B0),
                        onClick = { navController.navigate("transactions") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Loans",
                        icon = Icons.Default.AccountBalance,
                        color = Color(0xFFE91E63),
                        onClick = { navController.navigate("loans") }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}
