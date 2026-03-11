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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.enigma.fluffyinc.lists.domain.model.DiaryEntry
import com.enigma.fluffyinc.lists.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryListScreen(navController: NavController, vm: DiaryViewModel) {
    val entries by vm.diaryEntries.collectAsState()
    val now = System.currentTimeMillis()
    val upcoming = entries.filter { it.eventDate >= now }
    val past = entries.filter { it.eventDate < now }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diary / Planner", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.DiaryDetail.createRoute("new")) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.padding(horizontal = 16.dp)) {
            if (upcoming.isNotEmpty()) {
                item { SectionHeader("Upcoming Events") }
                items(upcoming, key = { it.id }) { entry ->
                    DiaryEntryCard(entry, vm, onClick = { navController.navigate(Screen.DiaryDetail.createRoute(entry.id)) })
                }
            }
            if (past.isNotEmpty()) {
                item { SectionHeader("Past Events") }
                items(past, key = { it.id }) { entry ->
                    DiaryEntryCard(entry, vm, onClick = { navController.navigate(Screen.DiaryDetail.createRoute(entry.id)) })
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun DiaryEntryCard(entry: DiaryEntry, vm: DiaryViewModel, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("EEE, MMM d yyyy  •  HH:mm", Locale.getDefault()) }
    val isPast = entry.eventDate < System.currentTimeMillis()
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPast) Color(0xFFF5F5F5) else Color(entry.color))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isPast) Color.Gray else Color.Unspecified)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(fmt.format(Date(entry.eventDate)), fontSize = 12.sp, color = Color.Gray)
                }
                if (entry.content.isNotBlank()) {
                    Text(entry.content.take(60) + if (entry.content.length > 60) "..." else "",
                        fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (entry.reminderEnabled && !isPast) {
                Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(diaryId: String, navController: NavController, vm: DiaryViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
    var reminderEnabled by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val isNew = diaryId == "new"
    val fmt = remember { SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(diaryId) {
        if (!isNew) {
            vm.getDiaryEntryById(diaryId)?.let {
                title = it.title
                content = it.content
                eventDate = it.eventDate
                reminderEnabled = it.reminderEnabled
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = eventDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        val cal = Calendar.getInstance().apply { timeInMillis = eventDate }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selected
                            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                        }
                        eventDate = newCal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = eventDate }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = Calendar.getInstance().apply {
                        timeInMillis = eventDate
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    eventDate = newCal.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Event" else "Edit Event", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = {
                        vm.upsertDiaryEntry(if (isNew) null else diaryId, title, content, eventDate, reminderEnabled)
                        navController.popBackStack()
                    }) { Icon(Icons.Default.Save, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Text("Event Date & Time", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(fmt.format(Date(eventDate)), fontSize = 13.sp)
                }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(timeFmt.format(Date(eventDate)), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("Notes / Description") },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                maxLines = 8
            )
            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (reminderEnabled) Color(0xFFFFF3E0) else Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NotificationsActive, null, tint = if (reminderEnabled) Color(0xFFFF5722) else Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reminders", fontWeight = FontWeight.SemiBold)
                        Text("Day before & on the day", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
            }
        }
    }
}
