package com.enigma.fluffyinc.apps.games.betweenle.ui

import androidx.compose.runtime.Composable
import com.enigma.fluffyinc.apps.games.betweenle.model.Stats
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
