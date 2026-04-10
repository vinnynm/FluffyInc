package com.enigma.fluffyinc.apps.finance.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.ScheduledPayment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledPaymentsScreen(viewModel: FinanceViewModel, navController: NavController) {
    val payments by viewModel.activePayments.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheduled Payments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Add Payment")
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(payments) { payment ->
                ScheduledPaymentCard(payment, viewModel)
            }
        }

        if (showDialog) {
            AddScheduledPaymentDialog(
                onDismiss = { showDialog = false },
                onConfirm = { payment ->
                    viewModel.addScheduledPayment(payment)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduledPaymentDialog(onDismiss: () -> Unit, onConfirm: (ScheduledPayment) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }
    var frequency by remember { mutableStateOf("Monthly") }
    var numberOfPayments by remember { mutableStateOf("") }
    var isIndefinite by remember { mutableStateOf(true) }
    var expandedFreq by remember { mutableStateOf(false) }
    var expandedCat by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val frequencies = listOf("Daily", "Weekly", "Monthly", "Annually")
    val categories = listOf("Rent", "Utilities", "Subscription", "Insurance", "Loan", "Other")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Scheduled Payment") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    label = { Text("Source (Bank/Card)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedFreq,
                    onExpandedChange = { expandedFreq = it }
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFreq) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFreq,
                        onDismissRequest = { expandedFreq = false }
                    ) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq) },
                                onClick = {
                                    frequency = freq
                                    expandedFreq = false
                                }
                            )
                        }
                    }
                }

                // Start Date Picker
                OutlinedTextField(
                    value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isIndefinite,
                        onCheckedChange = { isIndefinite = it }
                    )
                    Text("Indefinite (until cancelled)")
                }

                if (!isIndefinite) {
                    OutlinedTextField(
                        value = numberOfPayments,
                        onValueChange = { numberOfPayments = it },
                        label = { Text("Number of Payments") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { amt ->
                        onConfirm(ScheduledPayment(
                            amount = amt,
                            description = description,
                            source = source,
                            category = category,
                            frequency = frequency,
                            nextPaymentDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                            numberOfPayments = if (isIndefinite) null else numberOfPayments.toIntOrNull()
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
fun ScheduledPaymentCard(payment: ScheduledPayment, viewModel: FinanceViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(payment.description, style = MaterialTheme.typography.titleMedium)
                    Text("${payment.category} • ${payment.frequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary)
                    Text("Source: ${payment.source}", style = MaterialTheme.typography.bodySmall)
                    Text("Next: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(payment.nextPaymentDate))}",
                        style = MaterialTheme.typography.bodySmall)

                    payment.numberOfPayments?.let {
                        Text("${payment.paymentsMade}/$it payments made",
                            style = MaterialTheme.typography.bodySmall)
                    } ?: run {
                        Text("${payment.paymentsMade} payments made (Indefinite)",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        NumberFormat.getCurrencyInstance().format(payment.amount),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.cancelScheduledPayment(payment) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}