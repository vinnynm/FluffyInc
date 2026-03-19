package com.enigma.fluffyinc.apps.readables.epublibrary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enigma.fluffyinc.apps.readables.epublibrary.data.EpubContent
import com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel.ReaderTheme
import com.enigma.fluffyinc.readables.epublibrary.ui.components.ChapterListDialog
import com.enigma.fluffyinc.readables.epublibrary.ui.components.ChapterNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReaderScreen(
    paddingValues: PaddingValues,
    epubContent: EpubContent,
    currentChapterIndex: Int,
    fontSize: Int,
    theme: ReaderTheme,
    onCloseEpub: () -> Unit,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    onGoToChapter: (Int) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onThemeChange: (ReaderTheme) -> Unit
) {
    val currentChapter = epubContent.chapters.getOrNull(currentChapterIndex)
    val scrollState = rememberScrollState()
    var showChapterList by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Reset scroll when chapter changes
    LaunchedEffect(currentChapterIndex) {
        scrollState.scrollTo(0)
    }

    if (showChapterList) {
        ChapterListDialog (
            chapters = epubContent.chapters,
            onDismiss = { showChapterList = false },
            onChapterSelected = { index ->
                onGoToChapter(index)
                showChapterList = false
            }
        )
    }

    if (showSettings) {
        ReaderSettingsDialog(
            currentFontSize = fontSize,
            currentTheme = theme,
            onDismiss = { showSettings = false },
            onFontSizeChange = onFontSizeChange,
            onThemeChange = onThemeChange
        )
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = epubContent.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                        currentChapter?.let {
                            Text(
                                text = it.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCloseEpub) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Book"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reader Settings"
                        )
                    }
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Table of Contents"
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChapterNavigationBar(
                currentChapterIndex = currentChapterIndex,
                totalChapters = epubContent.chapters.size,
                onNext = onNextChapter,
                onPrevious = onPreviousChapter
            )
        }
    ) { innerPadding ->
        if (currentChapter != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                HtmlView(
                    htmlContent = currentChapter.content,
                    fontSize = fontSize,
                    theme = theme,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chapter not available.")
            }
        }
    }
}

@Composable
fun ReaderSettingsDialog(
    currentFontSize: Int,
    currentTheme: ReaderTheme,
    onDismiss: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onThemeChange: (ReaderTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reader Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Font Size: $currentFontSize%", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = currentFontSize.toFloat(),
                    onValueChange = { onFontSizeChange(it.toInt()) },
                    valueRange = 50f..250f,
                    steps = 20
                )

                Text("Theme", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ReaderTheme.values().forEach { theme ->
                        FilterChip(
                            selected = currentTheme == theme,
                            onClick = { onThemeChange(theme) },
                            label = { Text(theme.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
