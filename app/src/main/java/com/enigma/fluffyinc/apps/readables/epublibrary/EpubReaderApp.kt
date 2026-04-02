package com.enigma.fluffyinc.apps.readables.epublibrary


import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel.EpubReaderViewModel
import com.enigma.fluffyinc.apps.readables.epublibrary.ui.EpubListScreen
import com.enigma.fluffyinc.apps.readables.epublibrary.ui.EpubReaderScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EpubReaderApp(viewModel: EpubReaderViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages
    LaunchedEffect(Unit) {
        viewModel.errorFlow.collectLatest { message ->
            if (message.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Load saved settings and start initial scan
    LaunchedEffect(Unit) {
        viewModel.loadSettings(context)
        viewModel.setDirectoryAccess(true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.currentEpub != null -> {
                EpubReaderScreen (
                    paddingValues = paddingValues,
                    epubContent = uiState.currentEpub!!,
                    currentChapterIndex = uiState.currentChapterIndex,
                    fontSize = uiState.fontSize,
                    theme = uiState.theme,
                    onCloseEpub = { viewModel.closeEpub() },
                    onNextChapter = { viewModel.nextChapter() },
                    onPreviousChapter = { viewModel.previousChapter() },
                    onGoToChapter = { index -> viewModel.goToChapter(index) },
                    onFontSizeChange = { size -> viewModel.setFontSize(size, context) },
                    onThemeChange = { theme -> viewModel.setTheme(theme, context) }
                )
            }
            else -> {
                EpubListScreen(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    onOpenEpub = { epubFile -> viewModel.openEpub(epubFile, context) },
                    onToggleView = { viewModel.toggleViewMode(context) },
                    onScanDirectory = { uri -> viewModel.scanForEpubFiles(uri, context) },
                    onRemoveFolder = { uriString -> viewModel.removeFolder(uriString, context) },
                    onRefresh = { viewModel.refreshLibrary(context) }
                )
            }
        }
    }
}
