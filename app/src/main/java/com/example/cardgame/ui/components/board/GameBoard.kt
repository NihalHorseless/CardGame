package com.example.cardgame.ui.components.board

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.game.Board
import com.example.cardgame.game.GameManager

/**
 * Displays a unified 5x5 game board with both players' units.
 * The board has a neutral middle row (row 2).
 */
@Composable
fun GameBoard(
    gameBoard: Board,
    gameManager: GameManager,
    selectedCell: Pair<Int, Int>?,
    currentPlayerId: Int,
    validDeploymentPositions: List<Pair<Int, Int>> = emptyList(),
    validMoveDestinations: List<Pair<Int, Int>> = emptyList(),  // Add this parameter
    validAttackTargets: List<Pair<Int, Int>> = emptyList(),     // Add this parameter
    onCellClick: (row: Int, col: Int) -> Unit,
    registerCellPosition: (row: Int, col: Int, x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for highlighting
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Make it square
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF171C26),
                        Color(0xFF413973)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color(0xFF5271FF),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Create board rows
        for (row in 0 until gameBoard.rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Create cells in this row
                for (col in 0 until gameBoard.columns) {
                    val unit = gameBoard.getUnitAt(row, col)
                    val unitOwner = unit?.let { gameBoard.getUnitOwner(it) }
                    val isPlayerUnit = unitOwner == currentPlayerId
                    val isSelected = selectedCell == Pair(row, col)
                    val isNeutralZone = row == gameBoard.rows / 2 // Middle row (row 2) is neutral

                    // Check if this is a valid deployment/move/attack position
                    val isDeploymentPosition = validDeploymentPositions.contains(Pair(row, col))
                    val isMoveDestination = validMoveDestinations.contains(Pair(row, col))
                    val isAttackTarget = validAttackTargets.contains(Pair(row, col))

                    // Determine if unit can move or attack
                    val canMove = if (unit != null && isPlayerUnit) {
                        gameManager.movementManager.canUnitMove(unit)
                    } else false

                    val canAttack = if (unit != null && isPlayerUnit) {
                        unit.canAttackThisTurn
                    } else false

                    // Wrap with position tracker
                    CellPositionTracker(
                        row = row,
                        col = col,
                        registerPosition = registerCellPosition,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                    ) {
                        // Display cell with or without unit
                        UnifiedBoardCell(
                            unit = unit,
                            isSelected = isSelected,
                            isPlayerUnit = isPlayerUnit,
                            isNeutralZone = isNeutralZone,
                            isDeploymentZone = isInDeploymentZone(row, gameBoard.rows, unitOwner ?: -1),
                            isDeploymentPosition = isDeploymentPosition,
                            isMoveDestination = isMoveDestination,
                            isAttackTarget = isAttackTarget,
                            pulseAlpha = pulseAlpha,  // Pass animation state
                            canMove = canMove,
                            canAttack = canAttack,
                            onClick = { onCellClick(row, col) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// Update the UnifiedBoardCell to show all highlight types
@Composable
fun UnifiedBoardCell(
    unit: UnitCard?,
    isSelected: Boolean,
    isPlayerUnit: Boolean,
    isNeutralZone: Boolean,
    isDeploymentZone: Boolean,
    isDeploymentPosition: Boolean = false,
    isMoveDestination: Boolean = false,
    isAttackTarget: Boolean = false,
    pulseAlpha: Float = 0.6f,
    canMove: Boolean = false,
    canAttack: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Base cell background
    val cellBackground = when {
        isSelected -> Color(0xFFFFD700).copy(alpha = 0.3f) // Gold highlight for selected
        isDeploymentPosition -> Color(0x3300FF00) // Green highlight for deployment
        isNeutralZone -> Color(0xFF7A6F9B).copy(alpha = 0.3f) // Purple tint for neutral zone
        isDeploymentZone -> if (isPlayerUnit || unit == null)
            Color(0xFF1F3F77).copy(alpha = 0.3f) // Blue tint for player deployment zone
        else
            Color(0xFF771F1F).copy(alpha = 0.3f) // Red tint for opponent deployment zone
        else -> Color.Transparent
    }

    // Base border color and width
    val borderColor = when {
        isSelected -> Color(0xFFFFD700)
        isDeploymentPosition -> Color(0xFF00FF00) // Green border for deployment
        isAttackTarget -> Color(0xFFF44336) // Red border for attack targets
        isMoveDestination -> Color(0xFF3F51B5) // Blue border for movement
        else -> Color(0xFF5271FF).copy(alpha = 0.5f)
    }

    val borderWidth = when {
        isSelected -> 2.dp
        isDeploymentPosition || isAttackTarget || isMoveDestination -> 2.dp
        else -> 1.dp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(cellBackground)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Base cell content (unit or empty)
        if (unit != null) {
            // Use the enhanced UnitSlot component to display the unit with action indicators
            UnitSlot(
                unit = unit,
                isSelected = isSelected,
                isPlayerUnit = isPlayerUnit,
                canMove = canMove,
                canAttack = canAttack,
                onClick = onClick,
                modifier = Modifier.fillMaxSize(0.9f)
            )
        } else {
            // Empty cell - still clickable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() }
            )
        }

        // Draw move destination indicator (blue circle)
        if (isMoveDestination) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw a hollow blue circle
                drawCircle(
                    color = Color(0xFF3F51B5), // Blue
                    radius = size.minDimension * 0.3f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Add a small dot in the center
                drawCircle(
                    color = Color(0xFF3F51B5), // Blue
                    radius = size.minDimension * 0.08f,
                    center = center,
                    alpha = pulseAlpha
                )
            }
        }

        // Draw attack target indicator (red target)
        if (isAttackTarget) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension * 0.35f

                // Outer circle
                drawCircle(
                    color = Color(0xFFF44336), // Red
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Inner circle
                drawCircle(
                    color = Color(0xFFF44336), // Red
                    radius = radius * 0.6f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Center dot
                drawCircle(
                    color = Color(0xFFF44336), // Red
                    radius = size.minDimension * 0.06f,
                    center = center,
                    alpha = pulseAlpha
                )
            }
        }
    }
}

/**
 * Determines if a cell is in a player's deployment zone.
 * For a 5x5 board:
 * - Player 0's deployment zone is rows 3-4
 * - Player 1's deployment zone is rows 0-1
 * - Row 2 is neutral territory
 */
private fun isInDeploymentZone(row: Int, totalRows: Int, playerId: Int): Boolean {
    return when (playerId) {
        0 -> row > totalRows / 2 // Player 0's deployment zone is bottom half (rows 3-4)
        1 -> row < totalRows / 2 // Player 1's deployment zone is top half (rows 0-1)
        else -> false // Not a player's cell
    }
}