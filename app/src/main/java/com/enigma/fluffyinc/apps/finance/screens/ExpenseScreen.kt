package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.Expense
import com.enigma.fluffyinc.apps.finance.ui.component.ExpensePieChart
import com.enigma.fluffyinc.apps.finance.util.FinanceReportUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: FinanceViewModel, navController: NavController) {
    val expenseList by viewModel.allExpenses.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showChart by remember { mutableStateOf(true) }
    var showExportMenu by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    // Calculate date range (last 30 days)
    val endDate = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -30)
    val startDate = calendar.timeInMillis

    val expensesByCategory by viewModel.getExpensesByCategory(startDate, endDate).collectAsState(initial = emptyMap())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showChart = !showChart }) {
                        Icon(if (showChart) Icons.AutoMirrored.Filled.List else Icons.Default.PieChart, "Toggle View")
                    }
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, "Export")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as CSV") },
                                onClick = {
                                    showExportMenu = false
                                    FinanceReportUtils.exportExpensesToCsv(context, expenseList)?.let { uri ->
                                        FinanceReportUtils.shareFile(context, uri, "text/csv")
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.TableChart, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as PDF") },
                                onClick = {
                                    showExportMenu = false
                                    FinanceReportUtils.exportExpensesToPdf(context, expenseList)?.let { uri ->
                                        FinanceReportUtils.shareFile(context, uri, "application/pdf")
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Add Expense")
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showChart && expensesByCategory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Expenses by Category (Last 30 Days)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        ExpensePieChart(expensesByCategory)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenseList) { expense ->
                    ExpenseCard(expense, viewModel, navController)
                }
            }
        }

        if (showDialog) {
            AddExpenseDialog(
                onDismiss = { showDialog = false },
                onConfirm = { expense ->
                    viewModel.addExpense(expense)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (Expense) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Shopping", "Scheduled Payments", "Leisure", "Rent", "Food", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { amt ->
                        onConfirm(Expense(
                            amount = amt,
                            description = description,
                            source = source,
                            category = category
                        ))
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExpenseCard(expense: Expense, viewModel: FinanceViewModel, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (expense.shoppingListId != null) {
                    Modifier.clickable { navController.navigate("shopping/${expense.shoppingListId}") }
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.description, style = MaterialTheme.typography.titleMedium)
                    if (expense.shoppingListId != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Linked to shopping list",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(expense.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Text(expense.source, style = MaterialTheme.typography.bodySmall)
                Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date)),
                    style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance().format(expense.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
