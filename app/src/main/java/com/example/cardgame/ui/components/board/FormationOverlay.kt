package com.example.cardgame.ui.components.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.model.formation.Formation

/**
 * Visualizes active formations on the game board by drawing lines between units.
 *
 * @param activeFormations List of active formations to visualize
 * @param boardUnits List of units on the board (can be null for empty slots)
 * @param slotPositions Map of slot indices to their screen positions (if available)
 */
@Composable
fun FormationOverlay(
    activeFormations: List<Formation>,
    boardUnits: List<UnitCard?>,
    slotPositions: Map<Int, Offset> = emptyMap()
) {
    if (activeFormations.isEmpty()) return

    // If we don't have slot positions, we need to position formations based on estimated board layout
    val calculatedPositions = if (slotPositions.isEmpty()) {
        calculateDefaultSlotPositions(boardUnits.size)
    } else {
        slotPositions
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            activeFormations.forEach { formation ->
                // Only draw formations where all positions have units
                val formationPositions = formation.unitPositions.filter { pos ->
                    pos < boardUnits.size && boardUnits[pos] != null
                }

                if (formationPositions.size >= 2) { // Need at least 2 points to draw a line
                    val path = Path()

                    // Get screen positions for units in this formation
                    val points = formationPositions.mapNotNull { pos ->
                        calculatedPositions[pos]?.let { offset ->
                            // Convert to canvas coordinates
                            Offset(offset.x, offset.y)
                        }
                    }

                    if (points.isNotEmpty()) {
                        // Start the path at the first point
                        path.moveTo(points.first().x, points.first().y)

                        // Connect all points in the formation
                        points.drop(1).forEach { point ->
                            path.lineTo(point.x, point.y)
                        }

                        // Close the path for polygons (3+ points)
                        if (points.size > 2) {
                            path.close()
                        }

                        // Draw the formation with a semi-transparent line
                        drawPath(
                            path = path,
                            color = getFormationColor(formation.name),
                            style = Stroke(
                                width = 3f,
                                cap = StrokeCap.Round
                            ),
                            alpha = 0.6f
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculate positions for board slots in a standard layout
 * This is a fallback when actual positions aren't available
 */
private fun calculateDefaultSlotPositions(boardSize: Int): Map<Int, Offset> {
    val positions = mutableMapOf<Int, Offset>()
    val rows = (boardSize + 6) / 7 // Round up to nearest row (max 7 per row)
    val unitWidth = 80f
    val unitHeight = 100f
    val paddingH = 10f

    for (i in 0 until boardSize) {
        val row = i / 7
        val col = i % 7

        val x = col * (unitWidth + paddingH) + (unitWidth / 2)
        val y = row * (unitHeight + paddingH) + (unitHeight / 2)

        positions[i] = Offset(x, y)
    }

    return positions
}

/**
 * Return a consistent color for each formation type
 */
private fun getFormationColor(formationName: String): Color {
    return when (formationName.lowercase()) {
        "triangle" -> Color(0xFFFFD700) // Gold
        "line" -> Color(0xFF00FFFF) // Cyan
        "diamond" -> Color(0xFFFF00FF) // Magenta
        "square" -> Color(0xFF00FF00) // Green
        "v-shape" -> Color(0xFFFF6347) // Tomato
        else -> Color(0xFFFFFFFF) // White
    }
}