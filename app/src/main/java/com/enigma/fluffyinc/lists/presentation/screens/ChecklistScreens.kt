package com.enigma.fluffyinc.lists.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.domain.model.Checklist
import com.enigma.fluffyinc.lists.domain.model.ChecklistItem
import com.enigma.fluffyinc.lists.domain.model.EntryType
import com.enigma.fluffyinc.lists.presentation.components.EntryTagRow
import com.enigma.fluffyinc.lists.presentation.components.PdfExporter
import com.enigma.fluffyinc.lists.presentation.components.ReminderPanel
import com.enigma.fluffyinc.lists.presentation.components.sharePdf
import com.enigma.fluffyinc.lists.presentation.navigation.Screen

import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistListScreen(navController: NavController, vm: ChecklistViewModel) {
    val checklists by vm.checklists.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checklists", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.ChecklistDetail.createRoute("new")) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(checklists, key = { it.id }) { cl ->
                ChecklistCard(cl, vm, onClick = { navController.navigate(Screen.ChecklistDetail.createRoute(cl.id)) })
            }
        }
    }
}

@Composable
fun ChecklistCard(cl: Checklist, vm: ChecklistViewModel, onClick: () -> Unit) {
    val items = remember(cl.itemsJson) { vm.parseItems(cl.itemsJson) }
    val done = items.count { it.isChecked }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(cl.color))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cl.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (items.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { if (items.isEmpty()) 0f else done.toFloat() / items.size },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp),
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
                Text("$done / ${items.size} done", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistDetailScreen(
    checklistId: String,
    navController: NavController,
    vm: ChecklistViewModel,
    tagVm: TagViewModel,
    reminderVm: ReminderViewModel
) {
    var title by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var newItemText by remember { mutableStateOf("") }
    val isNew = checklistId == "new"
    val savedId = remember { if (isNew) UUID.randomUUID().toString() else checklistId }
    val context = LocalContext.current

    val allTags by tagVm.allTags.collectAsState()
    val entryTags by tagVm.getTagsForEntry(savedId).collectAsState(emptyList())
    val reminders by reminderVm.getRemindersForEntry(savedId).collectAsState(emptyList())

    LaunchedEffect(checklistId) {
        if (!isNew) {
            vm.getChecklistById(checklistId)?.let {
                title = it.title
                items = vm.parseItems(it.itemsJson)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Checklist" else "Edit Checklist", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = {
                            vm.getChecklistSync(checklistId)?.let { cl ->
                                val uri = PdfExporter.exportChecklist(context, cl, items)
                                sharePdf(context, uri)
                            }
                        }) { Icon(Icons.Default.PictureAsPdf, null) }
                    }
                    IconButton(onClick = {
                        vm.upsertChecklist(savedId, title, items)
                        navController.popBackStack()
                    }) { Icon(Icons.Default.Save, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Checklist Title") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            EntryTagRow(
                entryTags = entryTags, allTags = allTags,
                onAddTag = { tagVm.addTagToEntry(savedId, it.id, EntryType.CHECKLIST) },
                onRemoveTag = { tagVm.removeTagFromEntry(savedId, it.id) },
                onCreateTag = { name, color -> tagVm.createTag(name, color) }
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newItemText, onValueChange = { newItemText = it },
                    placeholder = { Text("Add item...") },
                    modifier = Modifier.weight(1f), singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    if (newItemText.isNotBlank()) {
                        items = items + ChecklistItem(UUID.randomUUID().toString(), newItemText.trim())
                        newItemText = ""
                    }
                }) { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary) }
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items, key = { it.id }) { item ->
                    ChecklistItemRow(
                        item = item,
                        onToggle = { items = items.map { if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it } },
                        onDelete = { items = items.filter { it.id != item.id } }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            ReminderPanel(
                reminders = reminders,
                onAdd = { at, rec, lbl ->
                    reminderVm.addReminder(
                        savedId,
                        EntryType.CHECKLIST,
                        at,
                        rec,
                        lbl
                    )
                },
                onToggle = { reminderVm.toggleReminder(it) },
                onDelete = { reminderVm.deleteReminder(it) }
            )
        }
    }
}

@Composable
fun ChecklistItemRow(item: ChecklistItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
        Text(
            item.text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
