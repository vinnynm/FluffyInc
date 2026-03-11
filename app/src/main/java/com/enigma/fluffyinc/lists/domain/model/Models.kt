package com.enigma.fluffyinc.lists.domain.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

// ─── Entry Type ────────────────────────────────────────────────────────────────

enum class EntryType { NOTE, CHECKLIST, LIST, DIARY, JOURNAL }

// ─── Tag ───────────────────────────────────────────────────────────────────────

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey val id: String,
    val name: String,
    val color: Long   // ARGB packed long, e.g. 0xFFE53935
)

// Entry ↔ Tag join table (works for all entry types)
@Entity(tableName = "entry_tags", primaryKeys = ["entryId", "tagId"])
data class EntryTag(
    val entryId: String,
    val tagId: String,
    val entryType: EntryType
)

// ─── Reminder ──────────────────────────────────────────────────────────────────

enum class RecurrenceType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val id: String,
    val entryId: String,
    val entryType: EntryType,
    val triggerAt: Long,                          // epoch ms for next fire
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val label: String = "",
    val isEnabled: Boolean = true
)

// ─── Note (with topics / subtopics) ────────────────────────────────────────────

data class TopicNode(
    val id: String,
    val title: String,
    val content: String = "",
    val children: List<TopicNode> = emptyList(),
    val depth: Int = 0
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,
    val title: String,
    val topicsJson: String = "[]",   // serialized List<TopicNode>
    val sketchJson: String = "[]",   // serialized List<SketchPath>
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFFFDE7
)

// ─── Sketch (Canvas drawing) ───────────────────────────────────────────────────

data class SketchPoint(val x: Float, val y: Float)

data class SketchPath(
    val id: String,
    val points: List<SketchPoint>,
    val colorArgb: Int = Color.BLACK,
    val strokeWidth: Float = 6f
)

// ─── Checklist ─────────────────────────────────────────────────────────────────

data class ChecklistItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)

// ─── Simple List ───────────────────────────────────────────────────────────────

data class ListItem(
    val id: String,
    val text: String
)

@Entity(tableName = "simple_lists")
data class SimpleList(
    @PrimaryKey val id: String,
    val title: String,
    val itemsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: Long = 0xFFE3F2FD
)

// ─── Diary ─────────────────────────────────────────────────────────────────────

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val eventDate: Long,              // the date of the planned event
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFCE4EC
)

// ─── Journal ───────────────────────────────────────────────────────────────────

enum class Emotion(val label: String, val color: Long, val icon: String) {
    HAPPY("Happy", 0xFFFFEB3B, "😊"),
    SAD("Sad", 0xFF90CAF9, "😢"),
    ANGRY("Angry", 0xFFEF9A9A, "😠"),
    ANXIOUS("Anxious", 0xFFCE93D8, "😰"),
    CALM("Calm", 0xFFA5D6A7, "😌"),
    EXCITED("Excited", 0xFFFFCC02, "🤩"),
    GRATEFUL("Grateful", 0xFFFFAB91, "🙏"),
    NEUTRAL("Neutral", 0xFFE0E0E0, "😐")
}

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val emotion: Emotion,
    val entryDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val biometricLock: Boolean = false
)

@Entity(tableName = "checklists")
data class Checklist(
    @PrimaryKey val id: String,
    val title: String,
    val itemsJson: String = "[]",   // serialized List<ChecklistItem>
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: Long = 0xFFE8F5E9
)
