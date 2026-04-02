package com.enigma.fluffyinc.apps.finance.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.finance.data.ShoppingItem
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(viewModel: FinanceViewModel, listId: Long, navController: NavController) {
    val items by viewModel.getShoppingItems(listId).collectAsState(initial = emptyList())
    val shoppingTypes by viewModel.shoppingTypes.collectAsState()
    val activeLists by viewModel.activeShoppingLists.collectAsState()
    val completedLists by viewModel.completedShoppingLists.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val currentList = remember(activeLists, completedLists, listId) {
        activeLists.find { it.id == listId } ?: completedLists.find { it.id == listId }
    }

    val isCompleted = currentList?.isCompleted ?: false
    val checkedItems = items.filter { it.isChecked }
    val uncheckedItems = items.filter { !it.isChecked }
    val checkedTotal = checkedItems.sumOf { it.quantity * it.pricePerItem }
    val totalItems = items.size
    val checkedCount = checkedItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentList?.name ?: "Shopping List")
                        if (isCompleted) {
                            Text(
                                "Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Text(
                                "$checkedCount of $totalItems items checked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isCompleted) {
                FloatingActionButton(onClick = { showAddItemDialog = true }) {
                    Icon(Icons.Default.Add, "Add Item")
                }
            }
        },
        bottomBar = {
            if (!isCompleted) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Checked Items Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    NumberFormat.getCurrencyInstance().format(checkedTotal),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = { showCompleteDialog = true },
                                enabled = checkedItems.isNotEmpty(),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(Icons.Default.Check, "Complete", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete List")
                            }
                        }

                        // Progress indicator
                        if (totalItems > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { checkedCount.toFloat() / totalItems.toFloat() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                // Show total for completed lists
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Spent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                NumberFormat.getCurrencyInstance().format(currentList?.totalAmount ?: 0.0),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            "Completed",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        if (items.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        "No items",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        if (isCompleted) "No items in this list" else "Add items to get started",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (!isCompleted) {
                        Button(onClick = { showAddItemDialog = true }) {
                            Icon(Icons.Default.Add, "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add First Item")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Show summary card if there are items
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$totalItems",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "Total Items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$checkedCount",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Checked",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    NumberFormat.getCurrencyInstance().format(items.sumOf { it.quantity * it.pricePerItem }),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "Total Value",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Section headers and items
                if (!isCompleted && uncheckedItems.isNotEmpty()) {
                    item {
                        Text(
                            "To Buy (${uncheckedItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(uncheckedItems) { item ->
                        ShoppingItemCard(item, viewModel, isCompleted)
                    }
                }

                if (checkedItems.isNotEmpty()) {
                    item {
                        Text(
                            if (isCompleted) "Items Purchased (${checkedItems.size})" else "Checked (${checkedItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    items(checkedItems) { item ->
                        ShoppingItemCard(item, viewModel, isCompleted)
                    }
                }
            }
        }

        // Add Item Dialog
        if (showAddItemDialog) {
            AddShoppingItemDialog(
                listId = listId,
                shoppingTypes = shoppingTypes,
                onDismiss = { showAddItemDialog = false },
                onConfirm = { item ->
                    viewModel.addShoppingItem(item)
                    showAddItemDialog = false
                }
            )
        }

        // Complete List Confirmation Dialog
        if (showCompleteDialog) {
            AlertDialog(
                onDismissRequest = { showCompleteDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, "Complete") },
                title = { Text("Complete Shopping List?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("This will:")
                        Text("• Add ${NumberFormat.getCurrencyInstance().format(checkedTotal)} to expenses")
                        Text("• Mark ${checkedItems.size} items as purchased")
                        if (uncheckedItems.isNotEmpty()) {
                            Text("• Move ${uncheckedItems.size} unchecked items to a new list")
                        }
                        Text("• Make this list read-only")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.completeShoppingList(listId)
                            showCompleteDialog = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("Complete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCompleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingItemDialog(
    listId: Long,
    shoppingTypes: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingItem) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pricePerItem by remember { mutableStateOf("") }
    var shoppingType by remember { mutableStateOf(shoppingTypes.firstOrNull() ?: "Groceries") }
    var expanded by remember { mutableStateOf(false) }
    var showNewTypeDialog by remember { mutableStateOf(false) }
    var newType by remember { mutableStateOf("") }
    val typesList = remember(shoppingTypes, newType) {
        if (newType.isNotBlank() && !shoppingTypes.contains(newType)) {
            shoppingTypes + newType
        } else shoppingTypes
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Shopping Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pricePerItem,
                    onValueChange = { pricePerItem = it },
                    label = { Text("Price per Item") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = shoppingType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Shopping Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            typesList.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        shoppingType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showNewTypeDialog = true }) {
                        Icon(Icons.Default.Add, "Add Type")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toIntOrNull()
                    val price = pricePerItem.toDoubleOrNull()
                    if (itemName.isNotBlank() && qty != null && price != null) {
                        onConfirm(ShoppingItem(
                            shoppingListId = listId,
                            itemName = itemName,
                            quantity = qty,
                            pricePerItem = price,
                            shoppingType = shoppingType
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

    if (showNewTypeDialog) {
        AlertDialog(
            onDismissRequest = { showNewTypeDialog = false },
            title = { Text("Add Shopping Type") },
            text = {
                OutlinedTextField(
                    value = newType,
                    onValueChange = { newType = it },
                    label = { Text("Type Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newType.isNotBlank()) {
                            shoppingType = newType
                            showNewTypeDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ShoppingItemCard(item: ShoppingItem, viewModel: FinanceViewModel, isCompleted: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCompleted) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = {
                        viewModel.updateShoppingItem(item.copy(isChecked = it))
                    }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
                Text("${item.quantity} x ${NumberFormat.getCurrencyInstance().format(item.pricePerItem)}",
                    style = MaterialTheme.typography.bodySmall)
                Text(item.shoppingType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance().format(item.quantity * item.pricePerItem),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!isCompleted) {
                    IconButton(onClick = { viewModel.deleteShoppingItem(item) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}