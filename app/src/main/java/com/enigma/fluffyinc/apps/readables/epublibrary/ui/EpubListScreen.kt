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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
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
    onScanDirectory: (Uri) -> Unit,
    onRemoveFolder: (String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    var showManageFolders by remember { mutableStateOf(false) }
    
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onScanDirectory(it)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            TopAppBar(
                title = { Text("EPUB Library") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showManageFolders = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "Manage Folders")
                    }
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
                    contentDescription = "Add Folder"
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
                            Text("Scanning library...")
                        }
                    }
                }
                uiState.epubFiles.isEmpty() && uiState.scannedFolders.isEmpty() -> {
                    EmptyLibraryView(
                        onSelectFolder = { directoryPickerLauncher.launch(null) }
                    )
                }
                uiState.epubFiles.isEmpty() && uiState.scannedFolders.isNotEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No EPUB files found in selected folders.")
                            Button(onClick = { directoryPickerLauncher.launch(null) }, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Add More Folders")
                            }
                        }
                    }
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
                                items = uiState.epubFiles,
                                key = { it.uri.toString() }
                            ) { epubFile ->
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

    if (showManageFolders) {
        AlertDialog(
            onDismissRequest = { showManageFolders = false },
            title = { Text("Managed Folders") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.scannedFolders.isEmpty()) {
                        Text("No folders added yet.")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(uiState.scannedFolders.toList()) { folderUri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = Uri.parse(folderUri).path ?: folderUri,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(onClick = { onRemoveFolder(folderUri) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageFolders = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { directoryPickerLauncher.launch(null) }) {
                    Text("Add Folder")
                }
            }
        )
    }
}
