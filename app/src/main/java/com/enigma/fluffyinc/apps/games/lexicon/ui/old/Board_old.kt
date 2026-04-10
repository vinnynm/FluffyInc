package com.enigma.fluffyinc.apps.games.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enigma.fluffyinc.apps.games.lexicon.model.PlacedTile
import com.enigma.fluffyinc.apps.games.lexicon.model.Tile

/**
@Composable
fun Board(
    board: Array<Array<Tile?>>,
    placedThisTurn: List<PlacedTile>,
    selectedTile: Int?,
    onCellClick: (Int, Int) -> Unit,
    cellSize: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        for (row in 0 until 15) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (col in 0 until 15) {
                    BoardCell(
                        row = row,
                        col = col,
                        tile = board[row][col],
                        isPlacedThisTurn = placedThisTurn.any { it.row == row && it.col == col },
                        onCellClick = onCellClick,
                        size = cellSize
                    )
                }
            }
        }
    }
}

@Composable
fun BoardCell(
    row: Int,
    col: Int,
    tile: Tile?,
    isPlacedThisTurn: Boolean,
    onCellClick: (Int, Int) -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    val isCenter = row == 7 && col == 7

    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                when {
                    tile != null -> Color(0xFFC8A96E)
                    isCenter -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = tile == null) { onCellClick(row, col) },
        contentAlignment = Alignment.Center
    ) {
        if (tile != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    tile.letter.toString(),
                    fontSize = (size.value * 0.4).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1208)
                )
                if (!tile.isBlank) {
                    Text(
                        tile.points.toString(),
                        fontSize = (size.value * 0.15).sp,
                        color = Color(0xFF1A1208).copy(alpha = 0.7f)
                    )
                }
            }
        } else if (isCenter) {
            Text("★", fontSize = (size.value * 0.3).sp)
        } else {
            val premium = when ("$row,$col") {
                "0,0", "0,7", "0,14", "7,0", "7,14", "14,0", "14,7", "14,14" -> "TW"
                "1,1", "2,2", "3,3", "4,4", "10,10", "11,11", "12,12", "13,13",
                "1,13", "2,12", "3,11", "4,10", "10,4", "11,3", "12,2", "13,1" -> "DW"
                "1,5", "1,9", "5,1", "5,5", "5,9", "5,13", "9,1", "9,5", "9,9", "9,13", "13,5", "13,9" -> "TL"
                else -> when ("$row,$col") {
                    "0,3", "0,11", "2,6", "2,8", "3,0", "3,7", "3,14", "6,2", "6,6", "6,8",
                    "6,12", "7,3", "7,11", "8,2", "8,6", "8,8", "8,12", "11,0", "11,7",
                    "11,14", "12,6", "12,8", "14,3", "14,11" -> "DL"
                    else -> null
                }
            }
            if (premium != null) {
                Text(
                    premium,
                    fontSize = (size.value * 0.2).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

*/