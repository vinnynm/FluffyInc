package com.enigma.fluffyinc.lists.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.domain.model.ListItem
import com.enigma.fluffyinc.lists.domain.model.SimpleList
import com.enigma.fluffyinc.lists.presentation.navigation.Screen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleListListScreen(navController: NavController, vm: SimpleListViewModel) {
    val lists by vm.lists.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lists", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) { Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null
                    ) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.SimpleListDetail.createRoute("new")) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(lists, key = { it.id }) { list ->
                SimpleListCard(list, vm, onClick = { navController.navigate(Screen.SimpleListDetail.createRoute(list.id)) })
            }
        }
    }
}

@Composable
fun SimpleListCard(list: SimpleList, vm: SimpleListViewModel, onClick: () -> Unit) {
    val items = remember(list.itemsJson) { vm.parseItems(list.itemsJson) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(list.color))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(list.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            items.take(3).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                    Spacer(Modifier.width(8.dp))
                    Text(item.text, fontSize = 13.sp, color = Color.DarkGray)
                }
            }
            if (items.size > 3) Text("+ ${items.size - 3} more", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleListDetailScreen(listId: String, navController: NavController, vm: SimpleListViewModel) {
    var title by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ListItem>>(emptyList()) }
    var newItemText by remember { mutableStateOf("") }
    val isNew = listId == "new"

    LaunchedEffect(listId) {
        if (!isNew) {
            vm.getListById(listId)?.let {
                title = it.title
                items = vm.parseItems(it.itemsJson)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New List" else "Edit List", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = {
                        vm.upsertList(if (isNew) null else listId, title, items)
                        navController.popBackStack()
                    }) { Icon(Icons.Default.Save, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("List Title") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newItemText, onValueChange = { newItemText = it },
                    placeholder = { Text("Add item...") },
                    modifier = Modifier.weight(1f), singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    if (newItemText.isNotBlank()) {
                        items = items + ListItem(UUID.randomUUID().toString(), newItemText.trim())
                        newItemText = ""
                    }
                }) { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary) }
            }

            Spacer(Modifier.height(12.dp))
            LazyColumn {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.", color = Color.Gray, modifier = Modifier.width(28.dp))
                        Text(item.text, modifier = Modifier.weight(1f))
                        IconButton(onClick = { items = items.filter { it.id != item.id } }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
