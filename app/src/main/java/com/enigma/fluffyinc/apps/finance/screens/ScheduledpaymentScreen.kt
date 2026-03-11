package com.enigma.fluffyinc.apps.finance.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import com.enigma.fluffyinc.apps.finance.data.ScheduledPayment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var frequency by remember { mutableStateOf("Monthly") }
    var numberOfPayments by remember { mutableStateOf("") }
    var isIndefinite by remember { mutableStateOf(true) }
    var expandedFreq by remember { mutableStateOf(false) }

    val frequencies = listOf("Weekly", "Monthly", "Annually")
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Scheduled Payment") },
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
                    expanded = expandedFreq,
                    onExpandedChange = { expandedFreq = it }
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFreq) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type= MenuAnchorType.PrimaryEditable, enabled = expandedFreq)
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
                            frequency = frequency,
                            nextPaymentDate = calendar.timeInMillis,
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
                    Text("${payment.frequency} - ${payment.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary)
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
