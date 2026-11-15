package com.enigma.fluffyinc.apps.games.absurdle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.enigma.fluffyinc.apps.games.absurdle.model.TileColor

// Absurdle uses a menacing purple-dark palette
val ColorGreen     = Color(0xFF538D4E)
val ColorYellow    = Color(0xFFB59F3B)
val ColorGray      = Color(0xFF3A3A3C)
val ColorBorder     = Color(0xFF3A3A5C)

// Accents now follow the app-wide (dynamic) colour scheme instead of a fixed purple.
val ColorPurple: Color @Composable get() = MaterialTheme.colorScheme.primary
val ColorPurpleLight: Color @Composable get() = MaterialTheme.colorScheme.primary
val ColorSubtle: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun AbsurdleTheme(content: @Composable () -> Unit) {
    // Uses the app-wide FluffyIncTheme; kept as a pass-through so call sites stay stable.
    content()
}

fun tileBackground(color: TileColor): Color = when (color) {
    TileColor.GREEN  -> ColorGreen
    TileColor.YELLOW -> ColorYellow
    TileColor.GRAY   -> ColorGray
    TileColor.EMPTY  -> Color.Transparent
}

fun tileBorder(color: TileColor): Color = when (color) {
    TileColor.EMPTY -> ColorBorder
    else            -> tileBackground(color)
}

fun keyBackground(color: TileColor): Color = when (color) {
    TileColor.GREEN  -> ColorGreen
    TileColor.YELLOW -> ColorYellow
    TileColor.GRAY   -> ColorGray
    TileColor.EMPTY  -> Color(0xFF565670)
}
