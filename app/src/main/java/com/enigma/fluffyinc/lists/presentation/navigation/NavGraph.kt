package com.enigma.fluffyinc.lists.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.enigma.fluffyinc.lists.di.AppModule
import com.enigma.fluffyinc.lists.presentation.screens.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NoteViewModel::class.java) ->
                NoteViewModel(AppModule.provideNoteRepository(context)) as T
            modelClass.isAssignableFrom(ChecklistViewModel::class.java) ->
                ChecklistViewModel(AppModule.provideChecklistRepository(context)) as T
            modelClass.isAssignableFrom(SimpleListViewModel::class.java) ->
                SimpleListViewModel(AppModule.provideSimpleListRepository(context)) as T
            modelClass.isAssignableFrom(DiaryViewModel::class.java) ->
                DiaryViewModel(AppModule.provideDiaryRepository(context), context) as T
            modelClass.isAssignableFrom(JournalViewModel::class.java) ->
                JournalViewModel(AppModule.provideJournalRepository(context)) as T
            modelClass.isAssignableFrom(TagViewModel::class.java) ->
                TagViewModel(AppModule.provideTagRepository(context)) as T
            modelClass.isAssignableFrom(ReminderViewModel::class.java) ->
                ReminderViewModel(AppModule.provideReminderRepository(context), context) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(
                    AppModule.provideNoteRepository(context),
                    AppModule.provideChecklistRepository(context),
                    AppModule.provideSimpleListRepository(context),
                    AppModule.provideDiaryRepository(context),
                    AppModule.provideJournalRepository(context),
                    AppModule.provideTagRepository(context)
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun NavGraphBuilder.listsGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Home.route, route = "lists_main") {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.NoteList.route) {
            val context = LocalContext.current
            val vm: NoteViewModel = viewModel(factory = ViewModelFactory(context))
            NoteListScreen(navController, vm)
        }

        composable(
            Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            val context = LocalContext.current
            val vm: NoteViewModel = viewModel(factory = ViewModelFactory(context))
            val tagVm: TagViewModel = viewModel(factory = ViewModelFactory(context))
            val reminderVm: ReminderViewModel = viewModel(factory = ViewModelFactory(context))
            NoteDetailScreen(
                noteId = it.arguments?.getString("noteId") ?: "new",
                navController = navController,
                vm = vm,
                tagVm = tagVm,
                reminderVm = reminderVm
            )
        }

        composable(Screen.ChecklistList.route) {
            val context = LocalContext.current
            val vm: ChecklistViewModel = viewModel(factory = ViewModelFactory(context))
            ChecklistListScreen(navController, vm)
        }

        composable(
            Screen.ChecklistDetail.route,
            arguments = listOf(navArgument("checklistId") { type = NavType.StringType })
        ) {
            val context = LocalContext.current
            val vm: ChecklistViewModel = viewModel(factory = ViewModelFactory(context))
            val tagVm: TagViewModel = viewModel(factory = ViewModelFactory(context))
            val reminderVm: ReminderViewModel = viewModel(factory = ViewModelFactory(context))
            ChecklistDetailScreen(
                checklistId = it.arguments?.getString("checklistId") ?: "new",
                navController = navController,
                vm = vm,
                tagVm = tagVm,
                reminderVm = reminderVm
            )
        }

        composable(Screen.SimpleListList.route) {
            val context = LocalContext.current
            val vm: SimpleListViewModel = viewModel(factory = ViewModelFactory(context))
            SimpleListListScreen(navController, vm)
        }

        composable(
            Screen.SimpleListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) {
            val context = LocalContext.current
            val vm: SimpleListViewModel = viewModel(factory = ViewModelFactory(context))
            SimpleListDetailScreen(it.arguments?.getString("listId") ?: "new", navController, vm)
        }

        composable(Screen.DiaryList.route) {
            val context = LocalContext.current
            val vm: DiaryViewModel = viewModel(factory = ViewModelFactory(context))
            DiaryListScreen(navController, vm)
        }

        composable(
            Screen.DiaryDetail.route,
            arguments = listOf(navArgument("diaryId") { type = NavType.StringType })
        ) {
            val context = LocalContext.current
            val vm: DiaryViewModel = viewModel(factory = ViewModelFactory(context))
            DiaryDetailScreen(it.arguments?.getString("diaryId") ?: "new", navController, vm)
        }

        composable(Screen.JournalList.route) {
            val context = LocalContext.current
            val vm: JournalViewModel = viewModel(factory = ViewModelFactory(context))
            JournalListScreen(navController, vm)
        }

        composable(
            Screen.JournalDetail.route,
            arguments = listOf(navArgument("journalId") { type = NavType.StringType })
        ) {
            val context = LocalContext.current
            val vm: JournalViewModel = viewModel(factory = ViewModelFactory(context))
            val tagVm: TagViewModel = viewModel(factory = ViewModelFactory(context))
            val reminderVm: ReminderViewModel = viewModel(factory = ViewModelFactory(context))
            JournalDetailScreen(
                journalId = it.arguments?.getString("journalId") ?: "new",
                navController = navController,
                vm = vm,
                tagVm = tagVm,
                reminderVm = reminderVm
            )
        }

        composable(Screen.Search.route) {
            val context = LocalContext.current
            val vm: SearchViewModel = viewModel(factory = ViewModelFactory(context))
            SearchScreen(navController, vm)
        }
    }
}
