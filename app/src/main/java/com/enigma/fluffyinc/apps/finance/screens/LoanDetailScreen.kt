package com.enigma.fluffyinc.apps.finance.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.LoanPayment
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(viewModel: FinanceViewModel, loanId: Long, navController: NavController) {
    val allLoans by viewModel.allLoans.collectAsState()
    val loan = allLoans.find { it.id == loanId }
    val payments by viewModel.getLoanPayments(loanId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showPayOffDialog by remember { mutableStateOf(false) }

    if (loan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loan not found")
        }
        return
    }

    val remainingPrincipal = loan.principalAmount - loan.amountPaid
    val remainingPayments = loan.loanTerm - loan.numberOfPaymentsMade
    val totalRemaining = loan.monthlyPayment * remainingPayments

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(loan.loanName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (loan.isActive) {
                        IconButton(onClick = { showPayOffDialog = true }) {
                            Icon(Icons.Default.CheckCircle, "Pay Off")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loan Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (loan.isActive)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Remaining Balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    NumberFormat.getCurrencyInstance().format(totalRemaining),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (loan.isActive) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Next Payment",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(loan.nextPaymentDate)),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Payment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    NumberFormat.getCurrencyInstance().format(loan.monthlyPayment),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Frequency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    loan.repaymentFrequency,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "$remainingPayments",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Loan Details Card
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Loan Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow("Principal Amount", NumberFormat.getCurrencyInstance().format(loan.principalAmount))
                        DetailRow("Interest Rate", "${loan.interestRate}% per year")
                        DetailRow("Total Interest", NumberFormat.getCurrencyInstance().format(loan.totalInterest))
                        DetailRow("Total Amount", NumberFormat.getCurrencyInstance().format(loan.totalAmount))
                        DetailRow("Amount Paid", NumberFormat.getCurrencyInstance().format(loan.amountPaid + (loan.monthlyPayment * loan.numberOfPaymentsMade)))
                        DetailRow("Loan Type", loan.loanType)
                        DetailRow("Start Date", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(loan.startDate)))
                        DetailRow("Status", if (loan.isActive) "Active" else "Completed")
                    }
                }
            }

            // Make Payment Button
            if (loan.isActive) {
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.makeLoanPayment(loanId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Payment, "Pay")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Make Payment (${NumberFormat.getCurrencyInstance().format(loan.monthlyPayment)})")
                    }
                }
            }

            // Payment History Header
            item {
                Text(
                    "Payment History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Empty State or Payment List
            if (payments.isEmpty()) {
                item {
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Receipt,
                                    "No payments",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No payments made yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            } else {
                items(payments) { payment ->
                    PaymentCard(payment)
                }
            }
        }

        // Pay Off Confirmation Dialog
        if (showPayOffDialog) {
            AlertDialog(
                onDismissRequest = { showPayOffDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, "Pay Off") },
                title = { Text("Pay Off Loan?") },
                text = {
                    Column {
                        Text("This will pay off the remaining principal:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            NumberFormat.getCurrencyInstance().format(remainingPrincipal),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This action cannot be undone.", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.payOffLoan(loanId)
                                showPayOffDialog = false
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Pay Off")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPayOffDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PaymentCard(payment: LoanPayment) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Payment #${payment.paymentNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.getDefault()
                    ).format(Date(payment.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Principal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            NumberFormat.getCurrencyInstance().format(payment.principalPortion),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Column {
                        Text(
                            "Interest",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            NumberFormat.getCurrencyInstance().format(payment.interestPortion),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    NumberFormat.getCurrencyInstance().format(payment.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}