package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.ShoppingList
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(viewModel: FinanceViewModel, navController: NavController) {
    val activeLists by viewModel.activeShoppingLists.collectAsState()
    val completedLists by viewModel.completedShoppingLists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showCompleted) "Completed Lists" else "Shopping Lists") },
                actions = {
                    IconButton(onClick = { showCompleted = !showCompleted }) {
                        Icon(if (showCompleted) Icons.AutoMirrored.Filled.List else Icons.Default.Check, "Toggle")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showCompleted) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, "New List")
                }
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
            val lists = if (showCompleted) completedLists else activeLists
            items(lists) { list ->
                ShoppingListCard(list, navController)
            }
        }

        if (showDialog) {
            var listName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Shopping List") },
                text = {
                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        label = { Text("List Name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (listName.isNotBlank()) {
                                viewModel.createShoppingList(listName)
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ShoppingListCard(list: ShoppingList, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("shopping/${list.id}") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(list.name, style = MaterialTheme.typography.titleMedium)
                if (list.isCompleted) {
                    list.completedDate?.let {
                        Text("Completed: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    list.totalAmount?.let {
                        Text("Total: ${NumberFormat.getCurrencyInstance().format(it)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Open")
        }
    }
}