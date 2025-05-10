package com.example.cardgame.ui.components.board

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.cardgame.data.enum.InteractionMode
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.FortificationCard
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
    validMoveDestinations: List<Pair<Int, Int>> = emptyList(),
    validAttackTargets: List<Pair<Int, Int>> = emptyList(),
    targetingType: TacticCardType? = null,
    interactionMode: InteractionMode = InteractionMode.DEFAULT,
    onCellClick: (row: Int, col: Int) -> Unit,
    onAttachBayonet: (row: Int, col: Int) -> Unit,
    visualHealthMap: Map<Card, Int> = emptyMap(),
    registerCellPosition: (row: Int, col: Int, x: Float, y: Float) -> Unit,
    entitiesInDeathAnimation: Set<Pair<Int, Int>> = emptySet(),
    modifier: Modifier = Modifier
) {
    // Animation state for pulsing effect
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
                    val currentPosition = Pair(row, col)
                    val isInDeathAnimation = currentPosition in entitiesInDeathAnimation

                    val unit = gameBoard.getUnitAt(row, col)
                    val unitOwner = unit?.let { gameBoard.getUnitOwner(it) }
                    val isPlayerUnit = unitOwner == currentPlayerId

                    // Add fortification handling
                    val fortification = gameBoard.getFortificationAt(row, col)
                    val fortificationOwner =
                        fortification?.let { gameBoard.getFortificationOwner(it) }
                    val isPlayerFortification = fortificationOwner == currentPlayerId

                    val isSelected = selectedCell == Pair(row, col)
                    val isNeutralZone = row == gameBoard.rows / 2 // Middle row (row 2) is neutral

                    // Check for highlighting types
                    val isDeploymentPosition = validDeploymentPositions.contains(
                        Pair(
                            row,
                            col
                        )
                    ) && unit == null && fortification == null
                    val isMoveDestination = validMoveDestinations.contains(Pair(row, col))
                    val isAttackTarget = validAttackTargets.contains(Pair(row, col))

                    // Determine if this position is a target based on the current interaction mode and targeting type
                    val isTargetPosition = validDeploymentPositions.contains(Pair(row, col)) &&
                            interactionMode == InteractionMode.CARD_TARGETING

                    // Determine the target indicator color based on the targeting type
                    val targetIndicatorColor = when (targetingType) {
                        TacticCardType.BUFF -> Color(0xFF4CAF50) // Green for buffs
                        TacticCardType.DIRECT_DAMAGE -> Color(0xFFF44336) // Red for direct damage
                        TacticCardType.AREA_EFFECT -> Color(0xFFFF9800) // Orange for area effects
                        else -> Color(0xFFF44336) // Default red
                    }

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
                        // Display cell with unit, fortification, or empty
                        UnifiedBoardCell(
                            unit = unit,
                            fortification = fortification,
                            isSelected = isSelected,
                            isPlayerUnit = isPlayerUnit,
                            isPlayerFortification = isPlayerFortification,
                            isNeutralZone = isNeutralZone,
                            isDeploymentZone = isInDeploymentZone(
                                row,
                                gameBoard.rows,
                                unitOwner ?: fortificationOwner ?: -1
                            ),
                            isDeploymentPosition = isDeploymentPosition,
                            isMoveDestination = isMoveDestination,
                            isAttackTarget = isAttackTarget ,
                            isTacticTarget = isTargetPosition,
                            isInDeathAnimation = isInDeathAnimation,
                            targetIndicatorColor = if (isTargetPosition) targetIndicatorColor else null,
                            visualHealthMap = visualHealthMap,
                            pulseAlpha = pulseAlpha,
                            canMove = canMove,
                            canAttack = canAttack,
                            onAttachBayonet = { onAttachBayonet(row, col) },
                            onClick = { onCellClick(row, col) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UnifiedBoardCell(
    unit: UnitCard?,
    fortification: FortificationCard?,
    isSelected: Boolean,
    isPlayerUnit: Boolean,
    isPlayerFortification: Boolean = false,
    isNeutralZone: Boolean,
    isDeploymentZone: Boolean,
    isDeploymentPosition: Boolean = false,
    isMoveDestination: Boolean = false,
    isAttackTarget: Boolean = false,
    isTacticTarget: Boolean = false,
    isInDeathAnimation: Boolean = false,
    targetIndicatorColor: Color? = null,
    pulseAlpha: Float = 0.6f,
    canMove: Boolean = false,
    canAttack: Boolean = false,
    onAttachBayonet: (() -> Unit)? = null,
    visualHealthMap: Map<Card, Int> = emptyMap(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Base cell background
    val cellBackground = when {
        isSelected -> Color(0xFFFFD700).copy(alpha = 0.3f) // Gold highlight for selected
        isDeploymentPosition -> Color(0x3300FF00) // Green highlight for deployment
        isInDeathAnimation -> Color(0x22FF0000)
        isNeutralZone -> Color(0xFF7A6F9B).copy(alpha = 0.3f) // Purple tint for neutral zone
        isDeploymentZone -> if ((isPlayerUnit && unit != null) || (isPlayerFortification && fortification != null) || (unit == null && fortification == null))
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
        isTacticTarget -> targetIndicatorColor?: Color.Transparent
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
        // Base cell content (unit, fortification, or empty)
        when {
            // If there's a unit, display it
            unit != null -> {
                UnitSlot(
                    unit = unit,
                    isSelected = isSelected,
                    isPlayerUnit = isPlayerUnit,
                    isDying = isInDeathAnimation,
                    canMove = canMove,
                    canAttack = canAttack,
                    onClick = onClick,
                    onAttachBayonet = onAttachBayonet,
                    visualHealth = visualHealthMap[unit],
                    modifier = Modifier.fillMaxSize(0.9f)
                )
            }
            // If there's a fortification, display it
            fortification != null -> {
                FortificationSlot(
                    fortification = fortification,
                    isSelected = isSelected,
                    isPlayerFortification = isPlayerFortification,
                    isFortFalling = isInDeathAnimation,
                    onClick = onClick,
                    visualHealth = visualHealthMap[fortification],
                    modifier = Modifier.fillMaxSize(0.9f)
                )
            }
            // Empty cell - still clickable
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClick() }
                )
            }
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
        if (isAttackTarget || isTacticTarget) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension * 0.35f
                val targetColor =
                    targetIndicatorColor ?: Color(0xFFF44336) // Use provided color or default red

                // Outer circle
                drawCircle(
                    color = targetColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Inner circle
                drawCircle(
                    color = targetColor,
                    radius = radius * 0.6f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Center dot
                drawCircle(
                    color = targetColor,
                    radius = size.minDimension * 0.06f,
                    center = center,
                    alpha = pulseAlpha
                )
            }
        }


        // Specifically for deployment positions, add a more distinctive indicator
        if (isDeploymentPosition) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Calculate size for the indicator - slightly smaller than the cell
                val size = Size(
                    width = this.size.width * 0.85f,
                    height = this.size.height * 0.85f
                )
                val topLeft = Offset(
                    x = (this.size.width - size.width) / 2,
                    y = (this.size.height - size.height) / 2
                )

                // Draw a semi-transparent green rectangle
                drawRect(
                    color = Color(0x3300FF00), // Translucent green
                    topLeft = topLeft,
                    size = size
                )

                // Draw dashed outline
                drawRect(
                    color = Color(0xFF00FF00), // Solid green
                    topLeft = topLeft,
                    size = size,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    ),
                    alpha = pulseAlpha
                )

                // Add a "+" symbol in the center
                val centerX = center.x
                val centerY = center.y
                val plusSize = size.minDimension * 0.3f

                // Horizontal line of the plus
                drawLine(
                    color = Color(0xFF00FF00),
                    start = Offset(centerX - plusSize / 2, centerY),
                    end = Offset(centerX + plusSize / 2, centerY),
                    strokeWidth = 3.dp.toPx(),
                    alpha = pulseAlpha
                )

                // Vertical line of the plus
                drawLine(
                    color = Color(0xFF00FF00),
                    start = Offset(centerX, centerY - plusSize / 2),
                    end = Offset(centerX, centerY + plusSize / 2),
                    strokeWidth = 3.dp.toPx(),
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