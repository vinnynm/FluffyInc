package com.enigma.fluffyinc.lists.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.enigma.fluffyinc.lists.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter fun fromTopicList(v: String): List<TopicNode> =
        gson.fromJson(v, object : TypeToken<List<TopicNode>>() {}.type) ?: emptyList()
    @TypeConverter fun toTopicList(v: List<TopicNode>): String = gson.toJson(v)

    @TypeConverter fun fromChecklistItems(v: String): List<ChecklistItem> =
        gson.fromJson(v, object : TypeToken<List<ChecklistItem>>() {}.type) ?: emptyList()
    @TypeConverter fun toChecklistItems(v: List<ChecklistItem>): String = gson.toJson(v)

    @TypeConverter fun fromListItems(v: String): List<ListItem> =
        gson.fromJson(v, object : TypeToken<List<ListItem>>() {}.type) ?: emptyList()
    @TypeConverter fun toListItems(v: List<ListItem>): String = gson.toJson(v)

    @TypeConverter fun fromSketchPaths(v: String): List<SketchPath> =
        gson.fromJson(v, object : TypeToken<List<SketchPath>>() {}.type) ?: emptyList()
    @TypeConverter fun toSketchPaths(v: List<SketchPath>): String = gson.toJson(v)

    @TypeConverter fun fromEmotion(v: String): Emotion = Emotion.valueOf(v)
    @TypeConverter fun toEmotion(v: Emotion): String = v.name

    @TypeConverter fun fromEntryType(v: String): EntryType = EntryType.valueOf(v)
    @TypeConverter fun toEntryType(v: EntryType): String = v.name

    @TypeConverter fun fromRecurrence(v: String): RecurrenceType = RecurrenceType.valueOf(v)
    @TypeConverter fun toRecurrence(v: RecurrenceType): String = v.name
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notes ADD COLUMN sketchJson TEXT NOT NULL DEFAULT '[]'")
        db.execSQL("ALTER TABLE journal_entries ADD COLUMN biometricLock INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS tags (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, color INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS entry_tags (entryId TEXT NOT NULL, tagId TEXT NOT NULL, entryType TEXT NOT NULL, PRIMARY KEY (entryId, tagId))")
        db.execSQL("CREATE TABLE IF NOT EXISTS reminders (id TEXT NOT NULL PRIMARY KEY, entryId TEXT NOT NULL, entryType TEXT NOT NULL, triggerAt INTEGER NOT NULL, recurrence TEXT NOT NULL DEFAULT 'NONE', label TEXT NOT NULL DEFAULT '', isEnabled INTEGER NOT NULL DEFAULT 1)")
    }
}

@Database(
    entities = [
        Note::class, Checklist::class, SimpleList::class,
        DiaryEntry::class, JournalEntry::class,
        Tag::class, EntryTag::class, Reminder::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun simpleListDao(): SimpleListDao
    abstract fun diaryDao(): DiaryDao
    abstract fun journalDao(): JournalDao
    abstract fun tagDao(): TagDao
    abstract fun reminderDao(): ReminderDao
}
