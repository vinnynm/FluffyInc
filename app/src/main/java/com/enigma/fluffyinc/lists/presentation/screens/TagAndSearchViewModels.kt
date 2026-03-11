package com.enigma.fluffyinc.lists.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.lists.data.repository.*
import com.enigma.fluffyinc.lists.domain.model.*
import com.enigma.fluffyinc.lists.workers.cancelReminderWork
import com.enigma.fluffyinc.lists.workers.scheduleReminderWork
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// ─── Tag ViewModel ─────────────────────────────────────────────────────────────

class TagViewModel(
    private val tagRepo: TagRepository
) : ViewModel() {

    val allTags: StateFlow<List<Tag>> = tagRepo.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTagsForEntry(entryId: String): Flow<List<Tag>> = tagRepo.getTagsForEntry(entryId)

    fun createTag(name: String, color: Long) = viewModelScope.launch {
        tagRepo.upsertTag(
            Tag(
                UUID.randomUUID().toString(), name, color
            )
        )
    }

    fun deleteTag(tag: Tag) = viewModelScope.launch { tagRepo.deleteTag(tag) }

    fun addTagToEntry(entryId: String, tagId: String, entryType: EntryType) = viewModelScope.launch {
        tagRepo.addEntryTag(
            EntryTag(
                entryId,
                tagId,
                entryType
            )
        )
    }

    fun removeTagFromEntry(entryId: String, tagId: String) = viewModelScope.launch {
        tagRepo.removeEntryTag(entryId, tagId)
    }

    fun setTagsForEntry(entryId: String, tagIds: Set<String>, entryType: EntryType) = viewModelScope.launch {
        tagRepo.removeAllTagsFromEntry(entryId)
        tagIds.forEach { tagRepo.addEntryTag(
            EntryTag(
                entryId,
                it,
                entryType
            )
        ) }
    }

    suspend fun getTagsOnce(entryId: String): List<Tag> = tagRepo.getTagsForEntryOnce(entryId)
}

// ─── Reminder ViewModel ────────────────────────────────────────────────────────

class ReminderViewModel(
    private val reminderRepo: ReminderRepository,
    private val context: Context
) : ViewModel() {

    fun getRemindersForEntry(entryId: String): Flow<List<Reminder>> =
        reminderRepo.getRemindersForEntry(entryId)

    fun addReminder(
        entryId: String,
        entryType: EntryType,
        triggerAt: Long,
        recurrence: RecurrenceType,
        label: String
    ) = viewModelScope.launch {
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            entryId = entryId,
            entryType = entryType,
            triggerAt = triggerAt,
            recurrence = recurrence,
            label = label,
            isEnabled = true
        )
        reminderRepo.upsertReminder(reminder)
        scheduleReminderWork(context, reminder)
    }

    fun toggleReminder(reminder: Reminder) = viewModelScope.launch {
        val updated = reminder.copy(isEnabled = !reminder.isEnabled)
        reminderRepo.upsertReminder(updated)
        if (updated.isEnabled) scheduleReminderWork(context, updated)
        else cancelReminderWork(context, updated.id)
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        cancelReminderWork(context, reminder.id)
        reminderRepo.deleteReminder(reminder)
    }

    fun deleteAllRemindersForEntry(entryId: String) = viewModelScope.launch {
        val reminders = reminderRepo.getRemindersForEntryOnce(entryId)
        reminders.forEach { cancelReminderWork(context, it.id) }
        reminderRepo.deleteAllRemindersForEntry(entryId)
    }
}

// ─── Search ViewModel ──────────────────────────────────────────────────────────

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val entryType: EntryType
)

class SearchViewModel(
    private val noteRepo: NoteRepository,
    private val checklistRepo: ChecklistRepository,
    private val listRepo: SimpleListRepository,
    private val diaryRepo: DiaryRepository,
    private val journalRepo: JournalRepository,
    private val tagRepo: TagRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val results: StateFlow<List<SearchResultItem>> = _results.asStateFlow()

    private val _selectedTagFilter = MutableStateFlow<Tag?>(null)
    val selectedTagFilter: StateFlow<Tag?> = _selectedTagFilter.asStateFlow()

    val allTags: StateFlow<List<Tag>> = tagRepo.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) {
        _query.value = q
        if (q.isBlank() && _selectedTagFilter.value == null) {
            _results.value = emptyList()
        } else {
            search()
        }
    }

    fun setTagFilter(tag: Tag?) {
        _selectedTagFilter.value = tag
        search()
    }

    private fun search() = viewModelScope.launch {
        val q = _query.value.trim()
        val tagFilter = _selectedTagFilter.value
        val items = mutableListOf<SearchResultItem>()

        if (q.isNotBlank()) {
            noteRepo.searchNotes(q).forEach {
                items.add(SearchResultItem(it.id, it.title, "Note", EntryType.NOTE))
            }
            checklistRepo.searchChecklists(q).forEach {
                items.add(SearchResultItem(it.id, it.title, "Checklist", EntryType.CHECKLIST))
            }
            listRepo.searchLists(q).forEach {
                items.add(SearchResultItem(it.id, it.title, "List", EntryType.LIST))
            }
            diaryRepo.searchDiary(q).forEach {
                items.add(SearchResultItem(it.id, it.title, "Diary · ${it.content.take(40)}", EntryType.DIARY))
            }
            journalRepo.searchJournal(q).forEach {
                items.add(SearchResultItem(it.id, it.title, "Journal · ${it.content.take(40)}", EntryType.JOURNAL))
            }
        }

        if (tagFilter != null) {
            val allIds = items.map { it.id }.toSet()
            val entryTags = tagRepo.getEntryTagsForEntries(
                if (allIds.isNotEmpty()) allIds.toList() else listOf("__none__")
            ).filter { it.tagId == tagFilter.id }.map { it.entryId }.toSet()

            val filtered = if (q.isBlank()) {
                val all = mutableListOf<SearchResultItem>()
                noteRepo.searchNotes("").forEach { all.add(SearchResultItem(it.id, it.title, "Note", EntryType.NOTE)) }
                checklistRepo.searchChecklists("").forEach { all.add(SearchResultItem(it.id, it.title, "Checklist", EntryType.CHECKLIST)) }
                listRepo.searchLists("").forEach { all.add(SearchResultItem(it.id, it.title, "List", EntryType.LIST)) }
                diaryRepo.searchDiary("").forEach { all.add(SearchResultItem(it.id, it.title, "Diary", EntryType.DIARY)) }
                journalRepo.searchJournal("").forEach { all.add(SearchResultItem(it.id, it.title, "Journal", EntryType.JOURNAL)) }
                all.filter { it.id in entryTags }
            } else {
                items.filter { it.id in entryTags }
            }
            _results.value = filtered
        } else {
            _results.value = items
        }
    }
}
