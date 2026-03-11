package com.enigma.fluffyinc.apps.finance.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.Transaction
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import hu.ma.charts.pie.data.PieChartEntry
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: FinanceViewModel, navController: NavController) {
    val income by viewModel.allIncome.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    var selectedDays by remember { mutableIntStateOf(30) }
    var showChart by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    val endDate = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -selectedDays)
    val startDate = calendar.timeInMillis

    // Filter transactions by date range
    val filteredIncome = income.filter { it.date in startDate..endDate }
    val chartIncome = remember(filteredIncome) {
        filteredIncome.map {
            PieChartEntry(label = AnnotatedString(
                it.description
            ), value =  it.amount.toFloat())
        }

    }
    val filteredExpenses = expenses.filter { it.date in startDate..endDate }

    // Combine and sort transactions
    val transactions = remember(filteredIncome, filteredExpenses) {
        val incomeTransactions = filteredIncome.map {
            Transaction.IncomeTransaction(it.date, it.amount, it.description, it)
        }
        val expenseTransactions = filteredExpenses.map {
            Transaction.ExpenseTransaction(it.date, it.amount, it.description, it)
        }
        (incomeTransactions + expenseTransactions).sortedByDescending { it.date }
    }

    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    // Prepare chart data


    LaunchedEffect(filteredIncome, filteredExpenses, selectedDays) {
        val dateGroupedIncome = filteredIncome.groupBy {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
        }.mapValues { it.value.sumOf { income -> income.amount } }

        val dateGroupedExpences = filteredExpenses.groupBy {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
        }.mapValues { it.value.sumOf { expenses -> expenses.amount } }

        modelProducer.runTransaction {
            lineSeries{
                series(
                    dateGroupedIncome.values.ifEmpty { listOf(0) }
                )
                series(
                    dateGroupedExpences.values.ifEmpty { listOf(0) }
                )
            }

        }

        val dateGroupedExpenses = filteredExpenses.groupBy {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
        }.mapValues { it.value.sumOf { expense -> expense.amount } }

        // Get all dates in range
        val allDates = mutableSetOf<String>()
        allDates.addAll(dateGroupedIncome.keys)
        allDates.addAll(dateGroupedExpenses.keys)
        val sortedDates = allDates.sorted()

        val incomeData = sortedDates.map { dateGroupedIncome[it] ?: 0.0 }
        val expenseData = sortedDates.map { dateGroupedExpenses[it] ?: 0.0 }




    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showChart = !showChart }) {
                        Icon(
                            if (showChart) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Filled.ShowChart,
                            "Toggle View"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Time range selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time Range:", style = MaterialTheme.typography.titleMedium)

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.menuAnchor(
                                type= MenuAnchorType.PrimaryEditable, expanded)
                        ) {
                            Text("Last $selectedDays days")
                            Icon(Icons.Default.ArrowDropDown, "Select")
                        }
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(7, 14, 30, 60, 90, 365).forEach { days ->
                                DropdownMenuItem(
                                    text = { Text("Last $days days") },
                                    onClick = {
                                        selectedDays = days
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Income", style = MaterialTheme.typography.bodySmall)
                        Text(
                            NumberFormat.getCurrencyInstance().format(filteredIncome.sumOf { it.amount }),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Expenses", style = MaterialTheme.typography.bodySmall)
                        Text(
                            NumberFormat.getCurrencyInstance().format(filteredExpenses.sumOf { it.amount }),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showChart && transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(250.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Income vs Expenses",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                layers = listOf(
                                    rememberColumnCartesianLayer(
                                        dataLabel = TextComponent(
                                            color = Color.Green.toArgb()
                                        ),
                                        columnCollectionSpacing = 0.dp,
                                        verticalAxisPosition = Axis.Position.Vertical.End
                                    ),
                                    rememberLineCartesianLayer(
                                        lineProvider = LineCartesianLayer.LineProvider.series(
                                            LineCartesianLayer.Line(
                                                fill = LineCartesianLayer.LineFill.double(
                                                    topFill = Fill(Color.Red.toArgb()),
                                                    bottomFill = Fill(Color.Blue.toArgb())
                                                ),
                                                pointConnector = LineCartesianLayer.PointConnector.cubic(),
                                                pointProvider = LineCartesianLayer.PointProvider.single(
                                                    point = LineCartesianLayer.Point(
                                                        component = LineComponent(
                                                            fill = Fill(Color.Red.toArgb()),
                                                            shape = com.patrykandpatrick.vico.core.common.shape.Shape.Rectangle
                                                        )
                                                    )
                                                ),
                                                dataLabel = TextComponent(
                                                    color = Color.Green.toArgb()
                                                ),
                                            ),
                                            LineCartesianLayer.Line(
                                                fill = LineCartesianLayer.LineFill.double(
                                                    topFill = Fill(Color.Magenta.toArgb()),
                                                    bottomFill = Fill(Color.Blue.toArgb())
                                                )
                                            )

                                        )
                                    )


                                    ).toTypedArray(),

                                ),
                            modelProducer = modelProducer,
                            modifier = Modifier.background(
                                color = Color.Gray
                            ),

                            )


                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Transactions list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (transaction) {
                is Transaction.IncomeTransaction -> Color(0xFF4CAF50).copy(alpha = 0.05f)
                is Transaction.ExpenseTransaction -> Color(0xFFF44336).copy(alpha = 0.05f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    when (transaction) {
                        is Transaction.IncomeTransaction -> Icons.AutoMirrored.Filled.TrendingUp
                        is Transaction.ExpenseTransaction -> Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = null,
                    tint = when (transaction) {
                        is Transaction.IncomeTransaction -> Color(0xFF4CAF50)
                        is Transaction.ExpenseTransaction -> Color(0xFFF44336)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (transaction) {
                                is Transaction.IncomeTransaction -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                is Transaction.ExpenseTransaction -> Color(0xFFF44336).copy(alpha = 0.1f)
                            },
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        transaction.description,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        when (transaction) {
                            is Transaction.IncomeTransaction -> transaction.income.category
                            is Transaction.ExpenseTransaction -> transaction.expense.category
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.date)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "${if (transaction is Transaction.IncomeTransaction) "+" else "-"}${NumberFormat.getCurrencyInstance().format(transaction.amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = when (transaction) {
                    is Transaction.IncomeTransaction -> Color(0xFF4CAF50)
                    is Transaction.ExpenseTransaction -> Color(0xFFF44336)
                }
            )
        }
    }
}