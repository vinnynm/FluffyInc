package com.enigma.fluffyinc.lists.data.local

import androidx.room.*
import com.enigma.fluffyinc.lists.domain.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :q || '%'")
    suspend fun searchNotes(q: String): List<Note>

    @Upsert
    suspend fun upsertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists ORDER BY updatedAt DESC")
    fun getAllChecklists(): Flow<List<Checklist>>

    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getChecklistById(id: String): Checklist?

    @Query("SELECT * FROM checklists WHERE title LIKE '%' || :q || '%'")
    suspend fun searchChecklists(q: String): List<Checklist>

    @Upsert
    suspend fun upsertChecklist(checklist: Checklist)

    @Delete
    suspend fun deleteChecklist(checklist: Checklist)
}

@Dao
interface SimpleListDao {
    @Query("SELECT * FROM simple_lists ORDER BY updatedAt DESC")
    fun getAllLists(): Flow<List<SimpleList>>

    @Query("SELECT * FROM simple_lists WHERE id = :id")
    suspend fun getListById(id: String): SimpleList?

    @Query("SELECT * FROM simple_lists WHERE title LIKE '%' || :q || '%'")
    suspend fun searchLists(q: String): List<SimpleList>

    @Upsert
    suspend fun upsertList(list: SimpleList)

    @Delete
    suspend fun deleteList(list: SimpleList)
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY eventDate ASC")
    fun getAllDiaryEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getDiaryEntryById(id: String): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE eventDate BETWEEN :start AND :end")
    suspend fun getDiaryEntriesInRange(start: Long, end: Long): List<DiaryEntry>

    @Query("SELECT * FROM diary_entries WHERE title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%'")
    suspend fun searchDiary(q: String): List<DiaryEntry>

    @Upsert
    suspend fun upsertDiaryEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntry)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY entryDate DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getJournalEntryById(id: String): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%'")
    suspend fun searchJournal(q: String): List<JournalEntry>

    @Upsert
    suspend fun upsertJournalEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntry)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT t.* FROM tags t INNER JOIN entry_tags et ON t.id = et.tagId WHERE et.entryId = :entryId")
    fun getTagsForEntry(entryId: String): Flow<List<Tag>>

    @Query("SELECT t.* FROM tags t INNER JOIN entry_tags et ON t.id = et.tagId WHERE et.entryId = :entryId")
    suspend fun getTagsForEntryOnce(entryId: String): List<Tag>

    @Upsert
    suspend fun upsertTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEntryTag(entryTag: EntryTag)

    @Query("DELETE FROM entry_tags WHERE entryId = :entryId AND tagId = :tagId")
    suspend fun removeEntryTag(entryId: String, tagId: String)

    @Query("DELETE FROM entry_tags WHERE entryId = :entryId")
    suspend fun removeAllTagsFromEntry(entryId: String)

    @Query("SELECT * FROM entry_tags WHERE entryId IN (:entryIds)")
    suspend fun getEntryTagsForEntries(entryIds: List<String>): List<EntryTag>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE entryId = :entryId")
    fun getRemindersForEntry(entryId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE entryId = :entryId")
    suspend fun getRemindersForEntryOnce(entryId: String): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): Reminder?

    @Upsert
    suspend fun upsertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE entryId = :entryId")
    suspend fun deleteAllRemindersForEntry(entryId: String)
}
