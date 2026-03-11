package com.enigma.fluffyinc.readables.epublibrary.ui.components

// FILE: com/example/epubreader/ui/components/ChapterNavigationBar.kt


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChapterNavigationBar(
    currentChapterIndex: Int,
    totalChapters: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrevious,
                enabled = currentChapterIndex > 0
            ) {
                Text("Previous")
            }
            Text(
                text = "${currentChapterIndex + 1} / $totalChapters",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onNext,
                enabled = currentChapterIndex < totalChapters - 1
            ) {
                Text("Next")
            }
        }
    }
}