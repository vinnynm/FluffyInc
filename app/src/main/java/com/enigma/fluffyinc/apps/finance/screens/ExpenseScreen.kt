package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.Expense
import com.enigma.fluffyinc.apps.finance.ui.component.ExpensePieChart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: FinanceViewModel, navController: NavController) {
    val expenseList by viewModel.allExpenses.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showChart by remember { mutableStateOf(true) }

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
                        Icon(if (showChart) Icons.AutoMirrored.Filled.List else Icons.Default.Star, "Toggle View")
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
                        .height(300.dp)
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
                    ExpenseCard(expense, viewModel)
                }
            }
        }

        if (showDialog) {
            AddExpenseDialog(
                onDismiss = {
                    showDialog = false
                },
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = expanded)
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
fun ExpenseCard(expense: Expense, viewModel: FinanceViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, style = MaterialTheme.typography.titleMedium)
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