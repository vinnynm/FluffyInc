package com.enigma.fluffyinc.apps.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class GameType(val title: String, val icon: ImageVector, val color: Color, val route: String) {
    data object ExplodingKittens : GameType("Exploding Kittens", Icons.Default.Pets, Color(0xFFFF1744), "exploding_kittens")
    data object WildTactics : GameType("Wild Tactics", Icons.Default.Casino, Color(0xFF4CAF50), "wild_tactics")
    data object PowerGame : GameType("Power Game", Icons.Default.Bolt, Color(0xFF2196F3), "power_game")
}

@Composable
fun GameDashboard(onGameSelected: (String) -> Unit) {
    val games = listOf(
        GameType.ExplodingKittens,
        GameType.WildTactics,
        GameType.PowerGame
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E))
                )
            )
            .padding(16.dp)
    ) {
        Text(
            "GAME HUB",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(games) { game ->
                GameCard(game = game, onClick = { onGameSelected(game.route) })
            }
        }
    }
}

@Composable
fun GameCard(game: GameType, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = game.color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, game.color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = game.icon,
                contentDescription = null,
                tint = game.color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = game.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
