package com.enigma.fluffyinc.apps.games.lexicon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.enigma.fluffyinc.ui.theme.Typography
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC8A96E),
    primaryContainer = Color(0xFF2A2418),
    secondary = Color(0xFF7EB8C8),
    secondaryContainer = Color(0xFF1A2A30),
    background = Color(0xFF0D0D0F),
    surface = Color(0xFF141418),
    surfaceVariant = Color(0xFF1C1C22),
    error = Color(0xFFE05C5C),
    onPrimary = Color(0xFF1A1208),
    onPrimaryContainer = Color(0xFFC8A96E),
    onSecondary = Color(0xFF1A1208),
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0),
    onSurfaceVariant = Color(0xFF6A6A80),
    outline = Color(0xFF2A2A35)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8A6A30),
    primaryContainer = Color(0xFFF5E6D0),
    secondary = Color(0xFF4A8A9A),
    secondaryContainer = Color(0xFFE0F0F5),
    background = Color(0xFFF5F5F0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8E8E0),
    error = Color(0xFFD32F2F),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF4A3A20),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFCCCCCC)
)

@Composable
fun LexiconTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val systemUiController = rememberSystemUiController()

    SideEffect {
        // Applying system bar settings similar to lexi_theme.xml
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
