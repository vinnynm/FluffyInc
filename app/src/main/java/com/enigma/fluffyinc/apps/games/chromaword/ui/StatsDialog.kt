package com.enigma.fluffyinc.apps.games.chromaword.ui

import androidx.compose.runtime.Composable
import com.enigma.fluffyinc.apps.games.chromaword.model.Stats
import com.enigma.fluffyinc.ui.core.GameStatsDialog

/** Thin adapter over the shared [GameStatsDialog]. */
@Composable
fun StatsDialog(stats: Stats, onDismiss: () -> Unit) {
    GameStatsDialog(
        gamesPlayed = stats.gamesPlayed,
        winRate = stats.winRate,
        currentStreak = stats.currentStreak,
        maxStreak = stats.maxStreak,
        guessDistribution = stats.guessDistribution,
        onDismiss = onDismiss
    )
}
