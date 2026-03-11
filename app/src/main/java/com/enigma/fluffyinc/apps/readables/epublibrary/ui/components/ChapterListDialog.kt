package com.enigma.fluffyinc.readables.epublibrary.ui.components

// FILE: com/example/epubreader/ui/components/ChapterListDialog.kt


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enigma.fluffyinc.apps.readables.epublibrary.data.Chapter

@Composable
fun ChapterListDialog(
    chapters: List<Chapter>,
    onDismiss: () -> Unit,
    onChapterSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Table of Contents") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(chapters) { index, chapter ->
                    Text(
                        text = chapter.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterSelected(index) }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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