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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.cardgame.ui.components.board.BattlefieldBackground
import com.example.cardgame.ui.components.board.GameBoard
import com.example.cardgame.ui.components.board.GameStatusBar
import com.example.cardgame.ui.components.board.PlayerPortrait
import com.example.cardgame.ui.components.effects.CardSlotAnimation
import com.example.cardgame.ui.components.effects.GifAttackAnimation
import com.example.cardgame.ui.components.effects.GifDeathAnimation
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
    val opponentName by viewModel.opponentName
    val playerVisualHealth by viewModel.playerVisualHealth
    val opponentVisualHealth by viewModel.opponentVisualHealth

    // Movement and attack highlighting
    val validMoveDestinations by viewModel.validMoveDestinations
    val validAttackTargets by viewModel.validAttackTargets

    // Animation states
    val isSimpleAttackVisible by viewModel.isSimpleAttackVisible
    val attackingUnitType by viewModel.attackingUnitType
    val attackTargetPosition by viewModel.attackTargetPosition
    val visualHealthMap by viewModel.visualHealthMap
    val isCardAnimationVisible by viewModel.isCardAnimationVisible
    val cardAnimationPosition by viewModel.cardAnimationPosition
    val isTacticEffectVisible by viewModel.isTacticEffectVisible
    val tacticEffectType by viewModel.tacticEffectType
    val tacticEffectPosition by viewModel.tacticEffectPosition
    val isDeathAnimationVisible by viewModel.isDeathAnimationVisible
    val deathEntityType by viewModel.deathEntityType
    val deathAnimationPosition by viewModel.deathAnimationPosition
    val entitiesInDeathAnimation by viewModel.entitiesInDeathAnimation
    // Deployment system states
    val selectedCardIndex by viewModel.selectedCardIndex
    val validDeploymentPositions by viewModel.validDeploymentPositions

    // Map to store cell positions for animations
    val cellPositionsMap = remember { mutableMapOf<Pair<Int, Int>, Pair<Float, Float>>() }

    LaunchedEffect(key1 = Unit) {
        if(!viewModel.isInCampaign.value)
        viewModel.startGame()
    }

    // Wrap everything in a Box to allow overlays
    Box(modifier = Modifier.fillMaxSize()) {
        // Main game content
        if (isGameOver) {
            GameOverScreen(
                isPlayerWinner = isPlayerWinner,
                onReturnToMainMenu = onNavigateToMain,
                onPlaySound = {
                    viewModel.playGameOverSound(isPlayerWinner = isPlayerWinner)
                }
            )
        } else {
            if(viewModel.isInCampaign.value) BattlefieldBackground()
            Column(
                modifier = if(!viewModel.isInCampaign.value) Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                else Modifier.fillMaxSize()
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
                ) {Log.d("Opponent", opponentHealth.toString())
                    // Opponent's portrait
                    PlayerPortrait(
                        playerName = opponentName,
                        health = opponentHealth,
                        maxHealth = opponentHealth,
                        visualHealth = opponentVisualHealth,
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
                        interactionMode = viewModel.interactionMode.value,
                        targetingType = viewModel.targetingType.value,
                        onCellClick = { row, col ->
                            viewModel.onCellClick(row, col)
                        },
                        onAttachBayonet = { row, col ->  // New parameter
                            viewModel.attachBayonet(row, col)
                        } ,
                        registerCellPosition = { row, col, x, y ->
                            viewModel.registerCellPosition(row, col, x, y)
                            cellPositionsMap[Pair(row, col)] = Pair(x, y)
                        },
                        visualHealthMap = visualHealthMap,
                        entitiesInDeathAnimation = entitiesInDeathAnimation,
                        modifier = Modifier.fillMaxSize()
                    )

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
                        visualHealth = playerVisualHealth,
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
                isVisible = true,
                targetX = attackTargetPosition.first,
                targetY = attackTargetPosition.second,
                onAnimationComplete = {
                    // Add later
                },
                modifier = Modifier.zIndex(10f)
            )
        }
        // Death animation layer
        if (isDeathAnimationVisible) {
            GifDeathAnimation(
                entityType = deathEntityType,
                isVisible = true,
                targetX = deathAnimationPosition.first,
                targetY = deathAnimationPosition.second,
                onAnimationComplete = { viewModel.onDeathAnimationComplete() },
                modifier = Modifier.fillMaxSize().zIndex(15f) // Higher zIndex to appear on top
            )
        }
        if (isTacticEffectVisible) {
            TacticCardEffectAnimation(
                isVisible = true,
                cardType = tacticEffectType,
                targetPosition = tacticEffectPosition,
                onAnimationComplete = { viewModel.onTacticEffectComplete() },
                modifier = Modifier.fillMaxSize().zIndex(10f)
            )
        }


        // Card play animation
        if (isCardAnimationVisible) {
            CardSlotAnimation(
                isVisible = true,
                targetX = cardAnimationPosition.first,
                targetY = cardAnimationPosition.second,
                onAnimationComplete = { /* Handled by ViewModel */ },
                modifier = Modifier.fillMaxSize().zIndex(10f)
            )
        }

    }
}