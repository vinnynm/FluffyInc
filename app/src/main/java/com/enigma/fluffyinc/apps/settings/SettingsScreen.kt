package com.enigma.fluffyinc.apps.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel.EpubReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, epubViewModel: EpubReaderViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by epubViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("EPUB Reader Folders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("These folders are automatically scanned for books.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            if (uiState.scannedFolders.isEmpty()) {
                item {
                    Text("No folders added yet.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(uiState.scannedFolders.toList().size) { index ->
                    val folderUri = uiState.scannedFolders.toList()[index]
                    FolderItem(
                        uriString = folderUri,
                        onRemove = { epubViewModel.removeFolder(folderUri, context) }
                    )
                }
            }
            
            item {
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("App Version", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("1.0.0 (Stable)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun FolderItem(uriString: String, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(
                text = uriString.substringAfterLast("%3A").replace("%2F", "/"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Remove", tint = Color.Red)
            }
        }
    }
}
