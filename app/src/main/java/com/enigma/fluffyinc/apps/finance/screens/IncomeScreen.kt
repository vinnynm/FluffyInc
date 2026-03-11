package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.Income
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(viewModel: FinanceViewModel, navController: NavController) {
    val incomeList by viewModel.allIncome.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Add Income")
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
            items(incomeList) { income ->
                IncomeCard(income, viewModel)
            }
        }

        if (showDialog) {
            AddIncomeDialog(
                onDismiss = { showDialog = false },
                onConfirm = { income ->
                    viewModel.addIncome(income)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeDialog(onDismiss: () -> Unit, onConfirm: (Income) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Salary") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Family and Friends", "Business", "Salary", "Donations", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Income") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(type= MenuAnchorType.PrimaryEditable, enabled= expanded)
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
                        onConfirm(Income(
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
fun IncomeCard(income: Income, viewModel: FinanceViewModel) {
    Card(modifier = Modifier
        .fillMaxWidth()

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.composite(
                        dstBrush = Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, Color.Black)
                        ),
                        srcBrush = Brush.horizontalGradient(
                            colors = listOf(Color.Green, MaterialTheme.colorScheme.secondary)
                        ),
                        blendMode = BlendMode.Softlight
                    )

                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)

            ) {
                Text(income.source, style = MaterialTheme.typography.titleMedium)
                Text(income.description, style = MaterialTheme.typography.bodySmall)
                Text(income.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(income.date)),
                    style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance().format(income.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { viewModel.deleteIncome(income) }) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}