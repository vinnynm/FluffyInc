package com.enigma.fluffyinc.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Shared "Statistics" dialog for the word games. Takes plain values so it works with
 * every game's own Stats model.
 */
@Composable
fun GameStatsDialog(
    gamesPlayed: Int,
    winRate: Float,
    currentStreak: Int,
    maxStreak: Int,
    guessDistribution: List<Int>,
    onDismiss: () -> Unit,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    AppDialog(title = "Statistics", onDismiss = onDismiss) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatNumber(gamesPlayed.toString(), "Played")
            StatNumber("${(winRate * 100).toInt()}%", "Win %")
            StatNumber(currentStreak.toString(), "Streak")
            StatNumber(maxStreak.toString(), "Best")
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            "Guess Distribution",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        val maxVal = max(1, guessDistribution.maxOrNull() ?: 1)
        guessDistribution.take(10).forEachIndexed { index, count ->
            DistributionBar(
                label = "${index + 1}",
                count = count,
                fraction = count.toFloat() / maxVal,
                color = barColor
            )
        }
    }
}

@Composable
private fun StatNumber(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DistributionBar(label: String, count: Int, fraction: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            modifier = Modifier.width(20.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val barWidth = maxWidth * fraction.coerceIn(0.05f, 1f)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (count > 0) {
                    Text(
                        text = " $count ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
