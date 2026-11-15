package com.enigma.fluffyinc.ui.core

import androidx.compose.ui.graphics.Color

/**
 * Semantic tile colours shared by all the word games. These are game *meaning*
 * (correct / present / absent), not app chrome, so they stay fixed regardless of
 * the dynamic colour scheme.
 */
object GameTileColors {
    val Correct = Color(0xFF538D4E)
    val Present = Color(0xFFB59F3B)
    val Absent = Color(0xFF3A3A3C)
}
