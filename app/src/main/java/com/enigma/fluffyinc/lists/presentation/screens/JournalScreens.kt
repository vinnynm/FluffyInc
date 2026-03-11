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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.domain.model.Emotion
import com.enigma.fluffyinc.lists.domain.model.EntryType
import com.enigma.fluffyinc.lists.domain.model.JournalEntry
import com.enigma.fluffyinc.lists.presentation.components.BiometricGate
import com.enigma.fluffyinc.lists.presentation.components.PdfExporter
import com.enigma.fluffyinc.lists.presentation.components.sharePdf
import com.enigma.fluffyinc.lists.presentation.navigation.Screen
import com.enigma.fluffyinc.lists.presentation.components.EntryTagRow
import com.enigma.fluffyinc.lists.presentation.components.ReminderPanel
import com.notesapp.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(navController: NavController, vm: JournalViewModel) {
    val entries by vm.journalEntries.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.JournalDetail.createRoute("new")) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.padding(horizontal = 16.dp)) {
            items(entries, key = { it.id }) { entry ->
                JournalEntryCard(entry, onClick = { navController.navigate(Screen.JournalDetail.createRoute(entry.id)) })
            }
        }
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault()) }
    val emotionColor = Color(entry.emotion.color)
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = emotionColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(emotionColor.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) { Text(entry.emotion.icon, fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(fmt.format(Date(entry.entryDate)), fontSize = 11.sp, color = Color.Gray)
                }
                if (entry.biometricLock) {
                    Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(emotionColor.copy(alpha = 0.5f)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text(entry.emotion.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
            }
            if (entry.content.isNotBlank() && !entry.biometricLock) {
                Spacer(Modifier.height(8.dp))
                Text(entry.content.take(80) + if (entry.content.length > 80) "..." else "", fontSize = 13.sp, color = Color.DarkGray)
            } else if (entry.biometricLock) {
                Spacer(Modifier.height(8.dp))
                Text("🔒 Content is locked", fontSize = 13.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    journalId: String,
    navController: NavController,
    vm: JournalViewModel,
    tagVm: TagViewModel,
    reminderVm: ReminderViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf(Emotion.NEUTRAL) }
    var biometricLock by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    val isNew = journalId == "new"
    val savedId = remember { if (isNew) java.util.UUID.randomUUID().toString() else journalId }

    val allTags by tagVm.allTags.collectAsState()
    val entryTags by tagVm.getTagsForEntry(savedId).collectAsState(emptyList())
    val reminders by reminderVm.getRemindersForEntry(savedId).collectAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(journalId) {
        if (!isNew) {
            vm.getJournalEntryById(journalId)?.let {
                title = it.title
                content = it.content
                selectedEmotion = it.emotion
                biometricLock = it.biometricLock
            }
        }
        loaded = true
    }

    val emotionColor = Color(selectedEmotion.color)

    BiometricGate(
        locked = biometricLock && !isNew && loaded,
        onUnlockSuccess = {},
        onDisableLock = { biometricLock = false }
    ) {
        Scaffold(
            containerColor = emotionColor.copy(alpha = 0.15f),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = emotionColor.copy(
                            alpha = 0.3f
                        )
                    ),
                    title = {
                        Text(
                            if (isNew) "New Journal Entry" else "Edit Entry",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null
                            )
                        }
                    },
                    actions = {
                        // PDF Export
                        if (!isNew) {
                            IconButton(onClick = {
                                vm.getJournalEntrySync(journalId)?.let { entry ->
                                    val uri = PdfExporter.exportJournalEntry(context, entry)
                                    sharePdf(context, uri)
                                }
                            }) { Icon(Icons.Default.PictureAsPdf, null) }
                        }
                        IconButton(onClick = {
                            vm.upsertJournalEntry(
                                savedId,
                                title,
                                content,
                                selectedEmotion,
                                biometricLock
                            )
                            navController.popBackStack()
                        }) { Icon(Icons.Default.Save, null) }
                    }
                )
            }
        ) { padding ->
            if (loaded) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Emotion Picker
                    Text("How are you feeling?", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    EmotionGrid(selected = selectedEmotion, onSelect = { selectedEmotion = it })
                    Spacer(Modifier.height(16.dp))

                    // Tags
                    EntryTagRow(
                        entryTags = entryTags,
                        allTags = allTags,
                        onAddTag = { tag ->
                            tagVm.addTagToEntry(
                                savedId,
                                tag.id,
                                EntryType.JOURNAL
                            )
                        },
                        onRemoveTag = { tag -> tagVm.removeTagFromEntry(savedId, tag.id) },
                        onCreateTag = { name, color -> tagVm.createTag(name, color) }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Entry Title") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = emotionColor,
                            focusedLabelColor = emotionColor
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = content, onValueChange = { content = it },
                        label = { Text("Write your thoughts...") },
                        modifier = Modifier.fillMaxWidth().height(200.dp), maxLines = 12,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = emotionColor,
                            focusedLabelColor = emotionColor
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    // Biometric lock toggle
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (biometricLock) Color(
                                0xFFEDE7F6
                            ) else Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (biometricLock) Icons.Default.Lock else Icons.Default.LockOpen,
                                null,
                                tint = if (biometricLock) Color(0xFF6650A4) else Color.Gray
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Biometric Lock", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Require fingerprint / PIN to view",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = biometricLock,
                                onCheckedChange = { biometricLock = it })
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Reminders
                    ReminderPanel(
                        reminders = reminders,
                        onAdd = { at, rec, lbl ->
                            reminderVm.addReminder(
                                savedId,
                                EntryType.JOURNAL,
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
    }
}

@Composable
fun EmotionGrid(selected: Emotion, onSelect: (Emotion) -> Unit) {
    val emotions = Emotion.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        emotions.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { emotion ->
                    EmotionChip(
                        emotion = emotion,
                        isSelected = emotion == selected,
                        onSelect = { onSelect(emotion) })
                }
            }
        }
    }
}

@Composable
fun EmotionChip(emotion: Emotion, isSelected: Boolean, onSelect: () -> Unit) {
    val color = Color(emotion.color)
    Card(
        modifier = Modifier
            .width(78.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.5f) else color.copy(alpha = 0.15f)
        ),
        border = if (isSelected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emotion.icon, fontSize = 22.sp)
            Text(emotion.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
