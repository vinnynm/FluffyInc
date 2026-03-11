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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.domain.model.EntryType
import com.enigma.fluffyinc.lists.domain.model.Note
import com.enigma.fluffyinc.lists.domain.model.SketchPath
import com.enigma.fluffyinc.lists.domain.model.TopicNode
import com.enigma.fluffyinc.lists.presentation.components.EntryTagRow
import com.enigma.fluffyinc.lists.presentation.components.PdfExporter
import com.enigma.fluffyinc.lists.presentation.components.ReminderPanel
import com.enigma.fluffyinc.lists.presentation.components.SketchCanvas
import com.enigma.fluffyinc.lists.presentation.components.sharePdf
import com.enigma.fluffyinc.lists.presentation.navigation.Screen
import com.notesapp.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

// ─── List ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(navController: NavController, vm: NoteViewModel) {
    val notes by vm.notes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(onClick = { navController.navigate(Screen.NoteDetail.createRoute("new")) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(notes, key = { it.id }) { note ->
                NoteCard(note = note, vm = vm, onClick = { navController.navigate(Screen.NoteDetail.createRoute(note.id)) })
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, vm: NoteViewModel, onClick: () -> Unit) {
    val topics = remember(note.topicsJson) { vm.parseTopics(note.topicsJson) }
    val fmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(note.color))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (topics.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("${topics.size} topic(s)", color = Color.Gray, fontSize = 12.sp)
            }
            Text(fmt.format(Date(note.updatedAt)), color = Color.Gray, fontSize = 11.sp)
        }
    }
}

// ─── Detail ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    navController: NavController,
    vm: NoteViewModel,
    tagVm: TagViewModel,
    reminderVm: ReminderViewModel
) {
    var title by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf<List<TopicNode>>(emptyList()) }
    var sketchPaths by remember { mutableStateOf<List<SketchPath>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }  // 0=topics 1=sketch 2=preview
    val isNew = noteId == "new"
    val savedId = remember { if (isNew) UUID.randomUUID().toString() else noteId }

    val allTags by tagVm.allTags.collectAsState()
    val entryTags by tagVm.getTagsForEntry(savedId).collectAsState(emptyList())
    val reminders by reminderVm.getRemindersForEntry(savedId).collectAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(noteId) {
        if (!isNew) {
            vm.getNoteById(noteId)?.let {
                title = it.title
                topics = vm.parseTopics(it.topicsJson)
                sketchPaths = vm.parseSketches(it.sketchJson)
            }
        }
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Note" else "Edit Note", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null
                        )
                    }
                },
                actions = {
                    // Export PDF
                    if (!isNew) {
                        IconButton(onClick = {
                            val uri = PdfExporter.exportNote(
                                context,
                                vm.getNoteSync(noteId)!!,
                                topics
                            )
                            sharePdf(context, uri)
                        }) { Icon(
                            Icons.Default.PictureAsPdf,
                            null
                        ) }
                    }
                    IconButton(onClick = {
                        vm.upsertNote(
                            savedId,
                            title,
                            topics,
                            sketchPaths
                        )
                        navController.popBackStack()
                    }) { Icon(
                        Icons.Default.Save,
                        null
                    ) }
                }
            )
        }
    ) { padding ->
        if (loaded) {
            Column(modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    singleLine = true
                )

                // Tags row
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    EntryTagRow(entryTags = entryTags, allTags = allTags, onAddTag = { tag ->
                            tagVm.addTagToEntry(
                                savedId,
                                tag.id,
                                EntryType.NOTE
                            )
                        }, onRemoveTag = { tag ->
                            tagVm.removeTagFromEntry(
                                savedId,
                                tag.id
                            )
                        }, onCreateTag = { name, color -> tagVm.createTag(name, color) })
                }

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Row(Modifier.padding(12.dp)) { Icon(Icons.Default.Topic, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Topics") }
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Row(Modifier.padding(12.dp)) { Icon(Icons.Default.Draw, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Sketch") }
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Row(Modifier.padding(12.dp)) { Icon(Icons.Default.Preview, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Preview") }
                    }
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                        Row(Modifier.padding(12.dp)) { Icon(Icons.Default.Alarm, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Reminders") }
                    }
                }

                when (selectedTab) {
                    0 -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        TopicTreeEditor(topics = topics, onTopicsChanged = { topics = it })
                    }
                    1 -> Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        SketchCanvas(paths = sketchPaths, onPathsChanged = { sketchPaths = it })
                    }
                    2 -> {
                        // Markdown preview of all topic content
                        val combined = remember(topics) { buildMarkdownFromTopics(topics) }
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                            if (combined.isBlank()) {
                                Text("No content to preview", color = Color.Gray)
                            } else {
                                MarkdownText(combined)
                            }
                        }
                    }
                    3 -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        ReminderPanel(
                            reminders = reminders,
                            onAdd = { at, rec, lbl ->
                                reminderVm.addReminder(
                                    savedId,
                                    EntryType.NOTE,
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
}

private fun buildMarkdownFromTopics(topics: List<TopicNode>, depth: Int = 0): String {
    val sb = StringBuilder()
    val prefix = "#".repeat((depth + 1).coerceAtMost(3)) + " "
    topics.forEach { node ->
        if (node.title.isNotBlank()) sb.append("$prefix${node.title}\n\n")
        if (node.content.isNotBlank()) sb.append("${node.content}\n\n")
        if (node.children.isNotEmpty()) sb.append(buildMarkdownFromTopics(node.children, depth + 1))
    }
    return sb.toString().trimEnd()
}

@Composable
fun TopicTreeEditor(
    topics: List<TopicNode>,
    onTopicsChanged: (List<TopicNode>) -> Unit,
    depth: Int = 0
) {
    Column {
        topics.forEachIndexed { index, topic ->
            TopicNodeEditor(
                node = topic,
                depth = depth,
                onNodeChanged = { updated ->
                    onTopicsChanged(topics.toMutableList().also { it[index] = updated })
                },
                onDelete = {
                    onTopicsChanged(topics.toMutableList().also { it.removeAt(index) })
                }
            )
        }
        if (depth < 3) {
            TextButton(onClick = {
                val newNode = TopicNode(
                    id = UUID.randomUUID().toString(), title = "", depth = depth
                )
                onTopicsChanged(topics + newNode)
            }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (depth == 0) "Add Topic" else "Add Subtopic", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun TopicNodeEditor(
    node: TopicNode,
    depth: Int,
    onNodeChanged: (TopicNode) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val indentPadding = (depth * 20).dp
    val bgColor = when (depth) {
        0 -> Color(0xFFEDE7F6); 1 -> Color(0xFFE8EAF6); else -> Color(0xFFE3F2FD)
    }

    Column(modifier = Modifier.padding(start = indentPadding)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (node.children.isNotEmpty()) {
                        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, modifier = Modifier.size(16.dp))
                        }
                    } else { Spacer(Modifier.width(24.dp)) }
                    OutlinedTextField(
                        value = node.title,
                        onValueChange = { onNodeChanged(node.copy(title = it)) },
                        placeholder = { Text(if (depth == 0) "Topic title..." else "Subtopic title...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f), singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = if (depth == 0) FontWeight.SemiBold else FontWeight.Normal)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
                OutlinedTextField(
                    value = node.content,
                    onValueChange = { onNodeChanged(node.copy(content = it)) },
                    placeholder = { Text("Content (supports **bold**, *italic*, `code`...)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    minLines = 1, maxLines = 4,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }
        }
        if (expanded) {
            TopicTreeEditor(topics = node.children, onTopicsChanged = { onNodeChanged(node.copy(children = it)) }, depth = depth + 1)
        }
    }
}
