package com.enigma.fluffyinc.readables.epublibrary.ui.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enigma.fluffyinc.apps.readables.epublibrary.data.EpubFile


@Composable
fun EpubGridItem(epubFile: EpubFile, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (epubFile.coverImage != null) {
                    Image(
                        bitmap = epubFile.coverImage,
                        contentDescription = epubFile.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "No Cover",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = epubFile.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = epubFile.author,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}