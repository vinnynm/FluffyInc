package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.enigma.fluffyinc.apps.finance.data.Loan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(viewModel: FinanceViewModel, navController: NavController) {
    val activeLoans by viewModel.activeLoans.collectAsState()
    val allLoans by viewModel.allLoans.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showActiveOnly by remember { mutableStateOf(true) }

    val displayLoans = if (showActiveOnly) activeLoans else allLoans

    // Calculate totals
    val totalLoanAmount = activeLoans.sumOf { it.principalAmount }
    val totalAmountToPay = activeLoans.sumOf {
        it.totalAmount - (it.amountPaid + (it.monthlyPayment * it.numberOfPaymentsMade))
    }
    val upcomingPayments = activeLoans.filter {
        it.nextPaymentDate <= System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // Next 7 days
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showActiveOnly = !showActiveOnly }) {
                        Icon(
                            if (showActiveOnly) Icons.Default.CheckCircle else Icons.Default.History,
                            "Toggle Active"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Add Loan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            "Upcoming",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${upcomingPayments.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Due Soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            "Total",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            NumberFormat.getCurrencyInstance().format(totalAmountToPay),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Upcoming Payments Section
            if (upcomingPayments.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Upcoming Payments (Next 7 Days)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        upcomingPayments.forEach { loan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    loan.loanName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(loan.nextPaymentDate)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loans List
            Text(
                if (showActiveOnly) "Active Loans" else "All Loans",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayLoans) { loan ->
                    LoanCard(loan, navController)
                }
            }
        }

        if (showDialog) {
            AddLoanDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, amount, rate, term, frequency, type ->
                    viewModel.addLoan(name, amount, rate, term, frequency, type)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Int, String, String) -> Unit
) {
    var loanName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var loanTerm by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Monthly") }
    var loanType by remember { mutableStateOf("Personal") }
    var expandedFreq by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    val frequencies = listOf("Weekly", "Monthly", "Annually")
    val types = listOf("Personal", "Business", "Mortgage", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Loan") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = loanName,
                    onValueChange = { loanName = it },
                    label = { Text("Loan Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Principal Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Annual Interest Rate (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = loanTerm,
                    onValueChange = { loanTerm = it },
                    label = { Text("Number of Payments") },
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
                        label = { Text("Payment Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFreq) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type= MenuAnchorType.PrimaryEditable, enabled= expandedFreq)
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

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                ) {
                    OutlinedTextField(
                        value = loanType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loan Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type= MenuAnchorType.PrimaryEditable, enabled = expandedType)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    loanType = type
                                    expandedType = false
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
                    val amt = amount.toDoubleOrNull()
                    val rate = interestRate.toDoubleOrNull()
                    val term = loanTerm.toIntOrNull()
                    if (loanName.isNotBlank() && amt != null && rate != null && term != null) {
                        onConfirm(loanName, amt, rate, term, frequency, loanType)
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
fun LoanCard(loan: Loan, navController: NavController) {
    val remainingAmount = loan.totalAmount - (loan.amountPaid + (loan.monthlyPayment * loan.numberOfPaymentsMade))
    val progress = (loan.amountPaid / loan.principalAmount).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("loan/${loan.id}") },
        colors = CardDefaults.cardColors(
            containerColor = if (loan.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(loan.loanName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${loan.loanType} • ${loan.interestRate}% • ${loan.repaymentFrequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (loan.isActive) {
                        Text(
                            "Next: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(loan.nextPaymentDate))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        NumberFormat.getCurrencyInstance().format(remainingAmount),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (loan.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "of ${NumberFormat.getCurrencyInstance().format(loan.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Progress: ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${loan.numberOfPaymentsMade}/${loan.loanTerm} payments",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}