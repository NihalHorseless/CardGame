package com.example.cardgame.ui.components.board

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onCellClick: (row: Int, col: Int) -> Unit,
    registerCellPosition: (row: Int, col: Int, x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
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

/**
 * A single cell in the unified game board.
 */
@Composable
fun UnifiedBoardCell(
    unit: UnitCard?,
    isSelected: Boolean,
    isPlayerUnit: Boolean,
    isNeutralZone: Boolean,
    isDeploymentZone: Boolean,
    canMove: Boolean = false,
    canAttack: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellBackground = when {
        isSelected -> Color(0xFFFFD700).copy(alpha = 0.3f) // Gold highlight for selected
        isNeutralZone -> Color(0xFF7A6F9B).copy(alpha = 0.3f) // Purple tint for neutral zone
        isDeploymentZone -> if (isPlayerUnit || unit == null)
            Color(0xFF1F3F77).copy(alpha = 0.3f) // Blue tint for player deployment zone
        else
            Color(0xFF771F1F).copy(alpha = 0.3f) // Red tint for opponent deployment zone
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(cellBackground)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF5271FF).copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
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