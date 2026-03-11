package com.enigma.fluffyinc.lists.data.repository

import com.enigma.fluffyinc.lists.data.local.*
import com.enigma.fluffyinc.lists.domain.model.*
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    suspend fun getNoteById(id: String): Note? = dao.getNoteById(id)
    suspend fun searchNotes(q: String): List<Note> = dao.searchNotes(q)
    suspend fun upsertNote(note: Note) = dao.upsertNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)
}

class ChecklistRepository(private val dao: ChecklistDao) {
    fun getAllChecklists(): Flow<List<Checklist>> = dao.getAllChecklists()
    suspend fun getChecklistById(id: String): Checklist? = dao.getChecklistById(id)
    suspend fun searchChecklists(q: String): List<Checklist> = dao.searchChecklists(q)
    suspend fun upsertChecklist(c: Checklist) = dao.upsertChecklist(c)
    suspend fun deleteChecklist(c: Checklist) = dao.deleteChecklist(c)
}

class SimpleListRepository(private val dao: SimpleListDao) {
    fun getAllLists(): Flow<List<SimpleList>> = dao.getAllLists()
    suspend fun getListById(id: String): SimpleList? = dao.getListById(id)
    suspend fun searchLists(q: String): List<SimpleList> = dao.searchLists(q)
    suspend fun upsertList(l: SimpleList) = dao.upsertList(l)
    suspend fun deleteList(l: SimpleList) = dao.deleteList(l)
}

class DiaryRepository(private val dao: DiaryDao) {
    fun getAllDiaryEntries(): Flow<List<DiaryEntry>> = dao.getAllDiaryEntries()
    suspend fun getDiaryEntryById(id: String): DiaryEntry? = dao.getDiaryEntryById(id)
    suspend fun getDiaryEntriesInRange(start: Long, end: Long) = dao.getDiaryEntriesInRange(start, end)
    suspend fun searchDiary(q: String): List<DiaryEntry> = dao.searchDiary(q)
    suspend fun upsertDiaryEntry(e: DiaryEntry) = dao.upsertDiaryEntry(e)
    suspend fun deleteDiaryEntry(e: DiaryEntry) = dao.deleteDiaryEntry(e)
}

class JournalRepository(private val dao: JournalDao) {
    fun getAllJournalEntries(): Flow<List<JournalEntry>> = dao.getAllJournalEntries()
    suspend fun getJournalEntryById(id: String): JournalEntry? = dao.getJournalEntryById(id)
    suspend fun searchJournal(q: String): List<JournalEntry> = dao.searchJournal(q)
    suspend fun upsertJournalEntry(e: JournalEntry) = dao.upsertJournalEntry(e)
    suspend fun deleteJournalEntry(e: JournalEntry) = dao.deleteJournalEntry(e)
}

class TagRepository(private val dao: TagDao) {
    fun getAllTags(): Flow<List<Tag>> = dao.getAllTags()
    fun getTagsForEntry(entryId: String): Flow<List<Tag>> = dao.getTagsForEntry(entryId)
    suspend fun getTagsForEntryOnce(entryId: String): List<Tag> = dao.getTagsForEntryOnce(entryId)
    suspend fun upsertTag(tag: Tag) = dao.upsertTag(tag)
    suspend fun deleteTag(tag: Tag) = dao.deleteTag(tag)
    suspend fun addEntryTag(entryTag: EntryTag) = dao.addEntryTag(entryTag)
    suspend fun removeEntryTag(entryId: String, tagId: String) = dao.removeEntryTag(entryId, tagId)
    suspend fun removeAllTagsFromEntry(entryId: String) = dao.removeAllTagsFromEntry(entryId)
    suspend fun getEntryTagsForEntries(entryIds: List<String>): List<EntryTag> =
        if (entryIds.isEmpty()) emptyList() else dao.getEntryTagsForEntries(entryIds)
}

class ReminderRepository(private val dao: ReminderDao) {
    fun getRemindersForEntry(entryId: String): Flow<List<Reminder>> = dao.getRemindersForEntry(entryId)
    suspend fun getRemindersForEntryOnce(entryId: String): List<Reminder> = dao.getRemindersForEntryOnce(entryId)
    suspend fun getReminderById(id: String): Reminder? = dao.getReminderById(id)
    suspend fun upsertReminder(reminder: Reminder) = dao.upsertReminder(reminder)
    suspend fun deleteReminder(reminder: Reminder) = dao.deleteReminder(reminder)
    suspend fun deleteAllRemindersForEntry(entryId: String) = dao.deleteAllRemindersForEntry(entryId)
}
