package com.enigma.fluffyinc.lists.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import com.enigma.fluffyinc.lists.domain.model.RecurrenceType
import com.enigma.fluffyinc.lists.domain.model.Tag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─── Tag Row (compact display + click to manage) ───────────────────────────────

@Composable
fun EntryTagRow(
    entryTags: List<Tag>,
    allTags: List<Tag>,
    onAddTag: (com.enigma.fluffyinc.lists.domain.model.Tag) -> Unit,
    onRemoveTag: (com.enigma.fluffyinc.lists.domain.model.Tag) -> Unit,
    onCreateTag: (String, Long) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(entryTags, key = { it.id }) { tag ->
                TagChipSmall(tag = tag, onRemove = { onRemoveTag(tag) })
            }
        }
        IconButton(onClick = { showSheet = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Filled.Label, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }

    if (showSheet) {
        TagPickerDialog(
            entryTags = entryTags,
            allTags = allTags,
            onAdd = onAddTag,
            onRemove = onRemoveTag,
            onCreateTag = onCreateTag,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
fun TagChipSmall(tag: com.enigma.fluffyinc.lists.domain.model.Tag, onRemove: (() -> Unit)? = null) {
    val color = Color(tag.color)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(tag.name, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        if (onRemove != null) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp).clickable { onRemove() }, tint = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPickerDialog(
    entryTags: List<com.enigma.fluffyinc.lists.domain.model.Tag>,
    allTags: List<com.enigma.fluffyinc.lists.domain.model.Tag>,
    onAdd: (com.enigma.fluffyinc.lists.domain.model.Tag) -> Unit,
    onRemove: (com.enigma.fluffyinc.lists.domain.model.Tag) -> Unit,
    onCreateTag: (String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(TAG_PALETTE.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Tags", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Existing tags to toggle
                if (allTags.isNotEmpty()) {
                    Text("Tags", fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    allTags.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                            row.forEach { tag ->
                                val isSelected = entryTags.any { it.id == tag.id }
                                TagPickerChip(tag = tag, isSelected = isSelected, onClick = {
                                    if (isSelected) onRemove(tag) else onAdd(tag)
                                })
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Spacer(Modifier.height(12.dp))
                }

                // Create new tag
                Text("New Tag", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTagName, onValueChange = { newTagName = it },
                    placeholder = { Text("Tag name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                // Color palette
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TAG_PALETTE.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(if (selectedColor == color) 2.dp else 0.dp, Color.Black, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            onCreateTag(newTagName.trim(), selectedColor)
                            newTagName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Create Tag") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
fun TagPickerChip(tag: com.enigma.fluffyinc.lists.domain.model.Tag, isSelected: Boolean, onClick: () -> Unit) {
    val color = Color(tag.color)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) color else color.copy(alpha = 0.15f))
            .border(1.dp, color, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp), tint = Color.White)
            Spacer(Modifier.width(4.dp))
        }
        Text(tag.name, fontSize = 12.sp, color = if (isSelected) Color.White else color, fontWeight = FontWeight.Medium)
    }
}

val TAG_PALETTE = listOf(
    0xFFE53935L, 0xFFE91E63L, 0xFF9C27B0L, 0xFF3F51B5L,
    0xFF2196F3L, 0xFF009688L, 0xFF4CAF50L, 0xFFFF9800L
)

// ─── Reminder Panel ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPanel(
    reminders: List<com.enigma.fluffyinc.lists.domain.model.Reminder>,
    onAdd: (triggerAt: Long, recurrence: com.enigma.fluffyinc.lists.domain.model.RecurrenceType, label: String) -> Unit,
    onToggle: (com.enigma.fluffyinc.lists.domain.model.Reminder) -> Unit,
    onDelete: (com.enigma.fluffyinc.lists.domain.model.Reminder) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Reminders", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", fontSize = 13.sp)
            }
        }

        reminders.forEach { reminder ->
            ReminderItem(reminder = reminder, onToggle = { onToggle(reminder) }, onDelete = { onDelete(reminder) }, fmt = fmt)
        }

        if (reminders.isEmpty()) {
            Text("No reminders set", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onConfirm = { triggerAt, recurrence, label ->
                onAdd(triggerAt, recurrence, label)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun ReminderItem(reminder: com.enigma.fluffyinc.lists.domain.model.Reminder, onToggle: () -> Unit, onDelete: () -> Unit, fmt: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (reminder.isEnabled) Color(0xFFFFF3E0) else Color(0xFFF5F5F5))
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (reminder.isEnabled) Icons.Default.Alarm else Icons.Default.AlarmOff,
                null,
                tint = if (reminder.isEnabled) Color(0xFFFF6F00) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (reminder.label.isBlank()) fmt.format(Date(reminder.triggerAt)) else reminder.label,
                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
                val recurrenceText = when (reminder.recurrence) {
                    com.enigma.fluffyinc.lists.domain.model.RecurrenceType.NONE -> fmt.format(Date(reminder.triggerAt))
                    com.enigma.fluffyinc.lists.domain.model.RecurrenceType.DAILY -> "Daily · ${fmt.format(Date(reminder.triggerAt))}"
                    com.enigma.fluffyinc.lists.domain.model.RecurrenceType.WEEKLY -> "Weekly · ${fmt.format(Date(reminder.triggerAt))}"
                    com.enigma.fluffyinc.lists.domain.model.RecurrenceType.MONTHLY -> "Monthly · ${fmt.format(Date(reminder.triggerAt))}"
                    com.enigma.fluffyinc.lists.domain.model.RecurrenceType.YEARLY -> "Yearly · ${fmt.format(Date(reminder.triggerAt))}"
                }
                if (reminder.label.isNotBlank()) Text(recurrenceText, fontSize = 11.sp, color = Color.Gray)
            }
            Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() }, modifier = Modifier.height(24.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onConfirm: (Long, com.enigma.fluffyinc.lists.domain.model.RecurrenceType, String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var selectedRecurrence by remember { mutableStateOf(com.enigma.fluffyinc.lists.domain.model.RecurrenceType.NONE) }
    var triggerAt by remember { mutableStateOf(System.currentTimeMillis() + 3600_000L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("EEE, MMM d yyyy · HH:mm", Locale.getDefault()) }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = triggerAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { sel ->
                        val cal = Calendar.getInstance().apply { timeInMillis = triggerAt }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                        }
                        triggerAt = newCal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = triggerAt }
        val state = rememberTimePickerState(initialHour = cal.get(Calendar.HOUR_OF_DAY), initialMinute = cal.get(Calendar.MINUTE))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Pick Time") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = Calendar.getInstance().apply {
                        timeInMillis = triggerAt
                        set(Calendar.HOUR_OF_DAY, state.hour)
                        set(Calendar.MINUTE, state.minute)
                    }
                    triggerAt = newCal.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text("Date & Time", fontSize = 13.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(triggerAt)), fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(triggerAt)), fontSize = 12.sp)
                    }
                }
                Text("Repeat", fontSize = 13.sp, color = Color.Gray)
                RecurrenceType.entries.forEach { rec ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedRecurrence = rec }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedRecurrence == rec, onClick = { selectedRecurrence = rec })
                        Text(rec.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(triggerAt, selectedRecurrence, label) }) { Text("Set Reminder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
