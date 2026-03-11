package com.enigma.fluffyinc.apps.readables.epublibrary.ui


import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.enigma.fluffyinc.apps.readables.epublibrary.data.EpubFile
import com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel.EpubReaderUiState
import com.enigma.fluffyinc.readables.epublibrary.ui.components.EpubGridItem
import com.enigma.fluffyinc.readables.epublibrary.ui.components.EpubListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubListScreen(
    paddingValues: PaddingValues,
    uiState: EpubReaderUiState,
    onOpenEpub: (EpubFile) -> Unit,
    onToggleView: () -> Unit,
    onScanDirectory: (Uri) -> Unit
) {
    val context = LocalContext.current
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Take persistable permissions
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Handle case where permission can't be taken
                e.printStackTrace()
            }
            onScanDirectory(it)
        }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            TopAppBar(
                title = { Text("EPUB Reader") },
                actions = {
                    if (uiState.epubFiles.isNotEmpty()) {
                        IconButton(onClick = onToggleView) {
                            Icon(
                                imageVector = if (uiState.isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                contentDescription = if (uiState.isGridView) "List View" else "Grid View"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { directoryPickerLauncher.launch(null) }
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Select Folder"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Scanning for EPUB files...")
                        }
                    }
                }
                uiState.epubFiles.isEmpty() -> {
                    EmptyLibraryView(
                        onSelectFolder = { directoryPickerLauncher.launch(null) }
                    )
                }
                else -> {
                    if (uiState.isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.epubFiles, key = { it.uri.toString() }) { epubFile ->
                                EpubGridItem (
                                    epubFile = epubFile,
                                    onClick = { onOpenEpub(epubFile) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                count = uiState.epubFiles.size,
                                key = { index -> uiState.epubFiles[index].uri.toString() }
                            ) { index ->
                                val epubFile = uiState.epubFiles[index]
                                EpubListItem(
                                    epubFile = epubFile,
                                    onClick = { onOpenEpub(epubFile) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
