package com.enigma.fluffyinc.ui.core

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Standard content card. Clickable when [onClick] is given. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
    if (onClick != null) {
        ElevatedCard(onClick = onClick, modifier = modifier, colors = colors) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        ElevatedCard(modifier = modifier, colors = colors) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}
