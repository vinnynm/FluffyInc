package com.enigma.fluffyinc.lists.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.lists.data.repository.ChecklistRepository
import com.enigma.fluffyinc.lists.data.repository.DiaryRepository
import com.enigma.fluffyinc.lists.data.repository.JournalRepository
import com.enigma.fluffyinc.lists.data.repository.NoteRepository
import com.enigma.fluffyinc.lists.data.repository.SimpleListRepository
import com.enigma.fluffyinc.lists.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.enigma.fluffyinc.lists.workers.cancelDiaryReminders
import com.enigma.fluffyinc.lists.workers.scheduleDiaryReminders
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// ─── Note VM ───────────────────────────────────────────────────────────────────

class NoteViewModel(
    private val repo: NoteRepository
) : ViewModel() {

    val notes: StateFlow<List<Note>> = repo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val gson = Gson()
    private var _cachedNote: Note? = null

    fun upsertNote(id: String, title: String, topics: List<TopicNode>, sketches: List<SketchPath> = emptyList()) = viewModelScope.launch {
        val note = Note(
            id = id,
            title = title,
            topicsJson = gson.toJson(topics),
            sketchJson = gson.toJson(sketches),
            updatedAt = System.currentTimeMillis()
        )
        _cachedNote = note
        repo.upsertNote(note)
    }

    suspend fun getNoteById(id: String): Note? = repo.getNoteById(id).also { _cachedNote = it }

    fun getNoteSync(id: String): Note? = _cachedNote

    fun parseTopics(json: String): List<TopicNode> =
        gson.fromJson(json, object : TypeToken<List<TopicNode>>() {}.type) ?: emptyList()

    fun parseSketches(json: String): List<SketchPath> =
        gson.fromJson(json, object : TypeToken<List<SketchPath>>() {}.type) ?: emptyList()

    fun deleteNote(note: Note) = viewModelScope.launch { repo.deleteNote(note) }
}

// ─── Checklist VM ──────────────────────────────────────────────────────────────

class ChecklistViewModel(
    private val repo: ChecklistRepository
) : ViewModel() {

    val checklists: StateFlow<List<Checklist>> = repo.getAllChecklists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val gson = Gson()
    private var _cachedChecklist: Checklist? = null

    fun upsertChecklist(id: String, title: String, items: List<ChecklistItem>) = viewModelScope.launch {
        val c = Checklist(
            id = id,
            title = title,
            itemsJson = gson.toJson(items),
            updatedAt = System.currentTimeMillis()
        )
        _cachedChecklist = c
        repo.upsertChecklist(c)
    }

    suspend fun getChecklistById(id: String): Checklist? = repo.getChecklistById(id).also { _cachedChecklist = it }

    fun getChecklistSync(id: String): Checklist? = _cachedChecklist

    fun parseItems(json: String): List<ChecklistItem> =
        gson.fromJson(json, object : TypeToken<List<ChecklistItem>>() {}.type) ?: emptyList()

    fun deleteChecklist(c: Checklist) = viewModelScope.launch { repo.deleteChecklist(c) }
}

// ─── SimpleList VM ─────────────────────────────────────────────────────────────

class SimpleListViewModel(
    private val repo: SimpleListRepository
) : ViewModel() {

    val lists: StateFlow<List<SimpleList>> = repo.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val gson = Gson()

    fun upsertList(id: String?, title: String, items: List<ListItem>) = viewModelScope.launch {
        val l = SimpleList(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            itemsJson = gson.toJson(items),
            updatedAt = System.currentTimeMillis()
        )
        repo.upsertList(l)
    }

    suspend fun getListById(id: String): SimpleList? = repo.getListById(id)

    fun parseItems(json: String): List<ListItem> =
        gson.fromJson(json, object : TypeToken<List<ListItem>>() {}.type) ?: emptyList()

    fun deleteList(l: SimpleList) = viewModelScope.launch { repo.deleteList(l) }
}

// ─── Diary VM ──────────────────────────────────────────────────────────────────

class DiaryViewModel(
    private val repo: DiaryRepository,
    private val context: Context
) : ViewModel() {

    val diaryEntries: StateFlow<List<DiaryEntry>> = repo.getAllDiaryEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsertDiaryEntry(
        id: String?, title: String, content: String,
        eventDate: Long, reminderEnabled: Boolean
    ) = viewModelScope.launch {
        val entryId = id ?: UUID.randomUUID().toString()
        val entry = DiaryEntry(
            id = entryId,
            title = title,
            content = content,
            eventDate = eventDate,
            reminderEnabled = reminderEnabled
        )
        repo.upsertDiaryEntry(entry)
        cancelDiaryReminders(context, entryId)
        if (reminderEnabled) scheduleDiaryReminders(context, entryId, title, eventDate)
    }

    suspend fun getDiaryEntryById(id: String): DiaryEntry? = repo.getDiaryEntryById(id)

    fun deleteDiaryEntry(e: DiaryEntry) = viewModelScope.launch {
        cancelDiaryReminders(context, e.id)
        repo.deleteDiaryEntry(e)
    }
}

// ─── Journal VM ────────────────────────────────────────────────────────────────

class JournalViewModel(
    private val repo: JournalRepository
) : ViewModel() {

    val journalEntries: StateFlow<List<JournalEntry>> = repo.getAllJournalEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var _cachedJournal: JournalEntry? = null

    fun upsertJournalEntry(
        id: String,
        title: String,
        content: String,
        emotion: Emotion,
        biometricLock: Boolean = false,
        entryDate: Long? = null,
        createdAt: Long? = null
    ) = viewModelScope.launch {
        val existing = repo.getJournalEntryById(id)
        val entry = JournalEntry(
            id = id,
            title = title,
            content = content,
            emotion = emotion,
            entryDate = entryDate ?: existing?.entryDate ?: System.currentTimeMillis(),
            createdAt = createdAt ?: existing?.createdAt ?: System.currentTimeMillis(),
            biometricLock = biometricLock
        )
        _cachedJournal = entry
        repo.upsertJournalEntry(entry)
    }

    suspend fun getJournalEntryById(id: String): JournalEntry? =
        repo.getJournalEntryById(id).also { _cachedJournal = it }

    fun getJournalEntrySync(id: String): JournalEntry? = _cachedJournal

    fun deleteJournalEntry(e: JournalEntry) = viewModelScope.launch { repo.deleteJournalEntry(e) }
}
