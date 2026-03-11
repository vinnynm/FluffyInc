package com.enigma.fluffyinc.lists.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enigma.fluffyinc.lists.domain.model.SketchPath
import com.enigma.fluffyinc.lists.domain.model.SketchPoint
import java.util.UUID

val SKETCH_COLOR_PALETTE = listOf(
    android.graphics.Color.BLACK,
    android.graphics.Color.parseColor("#E53935"),
    android.graphics.Color.parseColor("#1E88E5"),
    android.graphics.Color.parseColor("#43A047"),
    android.graphics.Color.parseColor("#FB8C00"),
    android.graphics.Color.parseColor("#8E24AA"),
    android.graphics.Color.WHITE
)

val STROKE_WIDTHS = listOf(3f, 6f, 10f, 18f)

@Composable
fun SketchCanvas(
    paths: List<SketchPath>,
    onPathsChanged: (List<SketchPath>) -> Unit,
    modifier: Modifier = Modifier,
    isEraser: Boolean = false
) {
    var selectedColor by remember { mutableStateOf(android.graphics.Color.BLACK) }
    var selectedStroke by remember { mutableStateOf(6f) }
    var eraserMode by remember { mutableStateOf(false) }
    var currentPoints by remember { mutableStateOf<List<SketchPoint>>(emptyList()) }

    Column(modifier = modifier) {
        // Toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Color palette
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Color", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp))
                    SKETCH_COLOR_PALETTE.forEach { color ->
                        val isSelected = !eraserMode && selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF6650A4) else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = color
                                    eraserMode = false
                                }
                        )
                    }
                }

                // Stroke width + eraser + undo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Size", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp))
                    STROKE_WIDTHS.forEach { w ->
                        val isSelected = !eraserMode && selectedStroke == w
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF6650A4) else Color(0xFFE0E0E0))
                                .clickable { selectedStroke = w; eraserMode = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size((w / 2).dp).clip(CircleShape).background(if (isSelected) Color.White else Color.DarkGray))
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Eraser toggle
                    IconButton(
                        onClick = { eraserMode = !eraserMode },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoFixNormal, null,
                            tint = if (eraserMode) Color(0xFF6650A4) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Undo
                    IconButton(
                        onClick = { if (paths.isNotEmpty()) onPathsChanged(paths.dropLast(1)) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }

                    // Clear
                    IconButton(
                        onClick = { onPathsChanged(emptyList()) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Drawing surface
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clipToBounds()
                .pointerInput(eraserMode, selectedColor, selectedStroke) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(SketchPoint(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            currentPoints = currentPoints + SketchPoint(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                if (eraserMode) {
                                    // Erase paths near this stroke
                                    val eraserPoints = currentPoints
                                    val remaining = paths.filter { path ->
                                        path.points.none { pt ->
                                            eraserPoints.any { ep ->
                                                val dx = pt.x - ep.x
                                                val dy = pt.y - ep.y
                                                dx * dx + dy * dy < 900f
                                            }
                                        }
                                    }
                                    onPathsChanged(remaining)
                                } else {
                                    val newPath = SketchPath(
                                        id = UUID.randomUUID().toString(),
                                        points = currentPoints,
                                        colorArgb = selectedColor,
                                        strokeWidth = selectedStroke
                                    )
                                    onPathsChanged(paths + newPath)
                                }
                                currentPoints = emptyList()
                            }
                        }
                    )
                }
        ) {
            // Draw committed paths
            paths.forEach { path -> drawSketchPath(path) }
            // Draw live path
            if (currentPoints.size > 1) {
                val color = if (eraserMode) android.graphics.Color.WHITE else selectedColor
                drawSketchPath(SketchPath("live", currentPoints, color, selectedStroke))
            }
        }
    }
}

private fun DrawScope.drawSketchPath(path: SketchPath) {
    if (path.points.size < 2) return
    val composeColor = Color(path.colorArgb)
    val androidPath = Path()
    androidPath.moveTo(path.points.first().x, path.points.first().y)
    path.points.drop(1).forEach { pt -> androidPath.lineTo(pt.x, pt.y) }
    drawPath(
        path = androidPath,
        color = composeColor,
        style = Stroke(
            width = path.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
