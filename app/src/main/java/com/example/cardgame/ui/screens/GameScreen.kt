package com.example.cardgame.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.cardgame.ui.components.board.GameBoard
import com.example.cardgame.ui.components.board.GameStatusBar
import com.example.cardgame.ui.components.board.PlayerPortrait
import com.example.cardgame.ui.components.effects.CardSlotAnimation
import com.example.cardgame.ui.components.effects.DamageNumberEffect
import com.example.cardgame.ui.components.effects.GifAttackAnimation
import com.example.cardgame.ui.components.effects.SimpleAttackAnimation
import com.example.cardgame.ui.components.effects.TacticCardEffectAnimation
import com.example.cardgame.ui.components.player.OpponentHand
import com.example.cardgame.ui.components.player.PlayerHand
import com.example.cardgame.ui.viewmodel.GameViewModel


@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToMain: () -> Unit
) {
    // Game state
    val playerHand by viewModel.playerHandState
    val gameBoardState by viewModel.gameBoardState
    val selectedCell by viewModel.selectedCell
    val playerMana by viewModel.playerMana
    val playerMaxMana by viewModel.playerMaxMana
    val playerHealth by viewModel.playerHealth
    val opponentHealth by viewModel.opponentHealth
    val opponentHandSize by viewModel.opponentHandSize
    val isGameOver by viewModel.isGameOver
    val isPlayerWinner by viewModel.isPlayerWinner
    val isPlayerTurn by viewModel.isPlayerTurn
    val statusMessage by viewModel.statusMessage

    // Movement and attack highlighting
    val validMoveDestinations by viewModel.validMoveDestinations
    val validAttackTargets by viewModel.validAttackTargets

    // Animation states
    val isSimpleAttackVisible by viewModel.isSimpleAttackVisible
    val attackingUnitType by viewModel.attackingUnitType
    val attackTargetPosition by viewModel.attackTargetPosition
    val isDamageNumberVisible by viewModel.isDamageNumberVisible
    val damageToShow by viewModel.damageToShow
    val damagePosition by viewModel.damagePosition
    val isHealingEffect by viewModel.isHealingEffect
    val isCardAnimationVisible by viewModel.isCardAnimationVisible
    val cardAnimationPosition by viewModel.cardAnimationPosition
    val isTacticEffectVisible by viewModel.isTacticEffectVisible
    val tacticEffectType by viewModel.tacticEffectType
    val tacticEffectPosition by viewModel.tacticEffectPosition

    // Movement animation
    val isUnitMovingAnimation by viewModel.isUnitMovingAnimation
    val moveStartPosition by viewModel.moveStartPosition
    val moveEndPosition by viewModel.moveEndPosition
    val movingUnitType by viewModel.movingUnitType

    // Deployment system states
    val selectedCardIndex by viewModel.selectedCardIndex
    val validDeploymentPositions by viewModel.validDeploymentPositions
    val interactionMode by viewModel.interactionMode

    // Map to store cell positions for animations
    val cellPositionsMap = mutableMapOf<Pair<Int, Int>, Pair<Float, Float>>()

    LaunchedEffect(key1 = Unit) {
        viewModel.startGame()
    }

    // Wrap everything in a Box to allow overlays
    Box(modifier = Modifier.fillMaxSize()) {
        // Main game content
        if (isGameOver) {
            GameOverScreen(
                isPlayerWinner = isPlayerWinner,
                onReturnToMainMenu = onNavigateToMain
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Status message
                if (statusMessage.isNotEmpty()) {
                    Log.d("StatusMessage",statusMessage)
                }

                // Top row with portraits
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Opponent's portrait
                    PlayerPortrait(
                        playerName = "Opponent",
                        health = opponentHealth,
                        maxHealth = 30,
                        isCurrentPlayer = !isPlayerTurn,
                        isTargetable = selectedCell != null &&
                                viewModel.playerContext.canAttackOpponentDirectly(
                                    selectedCell!!.first,
                                    selectedCell!!.second,
                                    viewModel.gameManager
                                ),
                        onPortraitClick = { viewModel.attackOpponentDirectly() },
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val bounds = coordinates.boundsInWindow()
                                val centerX = bounds.center.x
                                val centerY = bounds.center.y
                            }
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    // Opponent's Hand
                    OpponentHand(
                        cardCount = opponentHandSize,
                        modifier = Modifier.fillMaxWidth()
                    )

                }

                // Unified game board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    GameBoard(
                        gameBoard = viewModel.gameManager.gameBoard,
                        gameManager = viewModel.gameManager,
                        selectedCell = selectedCell,
                        currentPlayerId = 0, // Player's ID
                        validDeploymentPositions = validDeploymentPositions,
                        validMoveDestinations = validMoveDestinations,  // Pass move destinations
                        validAttackTargets = validAttackTargets,       // Pass attack targets
                        onCellClick = { row, col ->
                            viewModel.onCellClick(row, col)
                        },
                        registerCellPosition = { row, col, x, y ->
                            viewModel.registerCellPosition(row, col, x, y)
                            cellPositionsMap[Pair(row, col)] = Pair(x, y)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Highlight valid moves and attack targets
                    if (validMoveDestinations.isNotEmpty() || validAttackTargets.isNotEmpty()) {
                        /*
                        BoardHighlights(
                            validMoveDestinations = validMoveDestinations,
                            validAttackTargets = validAttackTargets,
                            cellPositions = cellPositionsMap,
                            modifier = Modifier.fillMaxSize().zIndex(5f)
                        )

                         */
                    }

                }
                // Game status (turn, mana)
                GameStatusBar(
                    playerMana = playerMana,
                    playerMaxMana = playerMaxMana,
                    isPlayerTurn = isPlayerTurn,
                    onEndTurn = { viewModel.endTurn() }
                )

                // Bottom row with player's portrait and hand
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player's portrait
                    PlayerPortrait(
                        playerName = "Player",
                        health = playerHealth,
                        maxHealth = 30,
                        isCurrentPlayer = isPlayerTurn,
                        isTargetable = false,
                        onPortraitClick = { /* No action needed */ },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            val centerX = bounds.center.x
                            val centerY = bounds.center.y
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Player's hand
                    PlayerHand(
                        cards = playerHand,
                        playerMana = playerMana,
                        selectedCardIndex = selectedCardIndex,  // Pass selected card index
                        onCardClick = { index -> viewModel.onCardSelected(index) },  // Use the new onCardSelected method
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Animation layers
        // Attack animation
        if (isSimpleAttackVisible) {
            GifAttackAnimation(
                unitType = attackingUnitType,
                isVisible = isSimpleAttackVisible,
                targetX = attackTargetPosition.first,
                targetY = attackTargetPosition.second,
                onAnimationComplete = {
                    // Add later
                },
                modifier = Modifier.zIndex(10f)
            )
        }
        if (isTacticEffectVisible) {
            TacticCardEffectAnimation(
                isVisible = isTacticEffectVisible,
                cardType = tacticEffectType,
                targetPosition = tacticEffectPosition,
                onAnimationComplete = { viewModel.onTacticEffectComplete() },
                modifier = Modifier.fillMaxSize().zIndex(10f)
            )
        }

        // Damage number animation
        if (isDamageNumberVisible) {
            DamageNumberEffect(
                damage = damageToShow,
                isHealing = isHealingEffect,
                x = damagePosition.first,
                y = damagePosition.second,
                isVisible = isDamageNumberVisible,
                onAnimationComplete = { /* Animation will be handled by ViewModel */ },
                modifier = Modifier.fillMaxSize().zIndex(10f)
            )
        }

        // Card play animation
        if (isCardAnimationVisible) {
            CardSlotAnimation(
                isVisible = isCardAnimationVisible,
                targetX = cardAnimationPosition.first,
                targetY = cardAnimationPosition.second,
                onAnimationComplete = { /* Handled by ViewModel */ },
                modifier = Modifier.fillMaxSize().zIndex(10f)
            )
        }

        // Unit movement animation
        if (isUnitMovingAnimation) {
        /*    UnitMovementAnimation(
                isVisible = isUnitMovingAnimation,
                unitType = movingUnitType,
                startX = moveStartPosition.first,
                startY = moveStartPosition.second,
                endX = moveEndPosition.first,
                endY = moveEndPosition.second,
                onAnimationComplete = { /* Handled by ViewModel */ },
                modifier = Modifier.fillMaxSize().zIndex(10f)
            )

         */
        }


    }
}