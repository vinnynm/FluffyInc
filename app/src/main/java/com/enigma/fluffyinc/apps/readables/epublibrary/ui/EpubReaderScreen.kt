package com.enigma.fluffyinc.apps.readables.epublibrary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enigma.fluffyinc.apps.readables.epublibrary.data.EpubContent
import com.enigma.fluffyinc.readables.epublibrary.ui.components.ChapterListDialog
import com.enigma.fluffyinc.readables.epublibrary.ui.components.ChapterNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReaderScreen(
    paddingValues: PaddingValues,
    epubContent: EpubContent,
    currentChapterIndex: Int,
    onCloseEpub: () -> Unit,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    onGoToChapter: (Int) -> Unit
) {
    val currentChapter = epubContent.chapters.getOrNull(currentChapterIndex)
    val scrollState = rememberScrollState()
    var showChapterList by remember { mutableStateOf(false) }

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
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chapter not available.")
            }
        }
    }
}