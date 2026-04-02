package com.enigma.fluffyinc.apps.finance.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.ui.component.ExpensePieChart
import java.text.NumberFormat
import java.util.*

enum class StatsPeriod(val label: String) {
    LAST_7_DAYS("7 Days"),
    LAST_MONTH("1 Month"),
    LAST_3_MONTHS("3 Months"),
    LAST_YEAR("1 Year")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingStatsScreen(viewModel: FinanceViewModel, navController: NavController) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.LAST_7_DAYS) }
    
    val startDate = remember(selectedPeriod) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            StatsPeriod.LAST_7_DAYS -> cal.add(Calendar.DAY_OF_YEAR, -7)
            StatsPeriod.LAST_MONTH -> cal.add(Calendar.MONTH, -1)
            StatsPeriod.LAST_3_MONTHS -> cal.add(Calendar.MONTH, -3)
            StatsPeriod.LAST_YEAR -> cal.add(Calendar.YEAR, -1)
        }
        cal.timeInMillis
    }
    val endDate = System.currentTimeMillis()

    val statsByType by viewModel.getShoppingExpensesByType(startDate, endDate).collectAsState(initial = emptyMap())
    val totalSpent = statsByType.values.sum()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Period Selector
            ScrollableTabRow(
                selectedTabIndex = selectedPeriod.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                StatsPeriod.entries.forEach { period ->
                    Tab(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        text = { Text(period.label) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Total Shopping Expenditure",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                NumberFormat.getCurrencyInstance().format(totalSpent),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (statsByType.isNotEmpty()) {
                    // Pie Chart Card
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Spending by Category",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                ExpensePieChart(statsByType)
                            }
                        }
                    }

                    // Breakdown
                    item {
                        Text(
                            "Category Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(statsByType.toList().sortedByDescending { it.second }) { (type, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.extraSmall
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(type, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                NumberFormat.getCurrencyInstance().format(amount),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider()
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No shopping data for this period",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
