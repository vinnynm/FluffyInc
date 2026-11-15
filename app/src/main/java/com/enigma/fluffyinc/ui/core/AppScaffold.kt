package com.enigma.fluffyinc.ui.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Standard screen frame: themed background, [AppTopBar], optional FAB / bottom bar.
 * Every feature screen should use this instead of its own Scaffold + TopAppBar.
 */
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = title, onBack = onBack, actions = actions) },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = content
    )
}
