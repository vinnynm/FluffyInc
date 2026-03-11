package com.enigma.fluffyinc.lists.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("lists_home")
    object NoteList : Screen("note_list")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(id: String) = "note_detail/$id"
    }
    object ChecklistList : Screen("checklist_list")
    object ChecklistDetail : Screen("checklist_detail/{checklistId}") {
        fun createRoute(id: String) = "checklist_detail/$id"
    }
    object SimpleListList : Screen("simple_list_list")
    object SimpleListDetail : Screen("simple_list_detail/{listId}") {
        fun createRoute(id: String) = "simple_list_detail/$id"
    }
    object DiaryList : Screen("diary_list")
    object DiaryDetail : Screen("diary_detail/{diaryId}") {
        fun createRoute(id: String) = "diary_detail/$id"
    }
    object JournalList : Screen("journal_list")
    object JournalDetail : Screen("journal_detail/{journalId}") {
        fun createRoute(id: String) = "journal_detail/$id"
    }
    object Search : Screen("search")
}
