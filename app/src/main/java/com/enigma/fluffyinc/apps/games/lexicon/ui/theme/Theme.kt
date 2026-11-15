package com.enigma.fluffyinc.apps.games.lexicon.ui.theme

import androidx.compose.runtime.Composable

/**
 * Lexicon now uses the app-wide FluffyIncTheme (dynamic colour). Kept as a
 * pass-through so existing call sites stay stable.
 */
@Composable
fun LexiconTheme(content: @Composable () -> Unit) {
    content()
}
