package com.example.cardgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.cardgame.ui.components.board.BoardWithFormationTracking
import com.example.cardgame.ui.components.board.GameStatusBar
import com.example.cardgame.ui.components.board.PlayerPortrait
import com.example.cardgame.ui.components.effects.AttackAnimation
import com.example.cardgame.ui.components.effects.DamageNumberEffect
import com.example.cardgame.ui.components.effects.SimpleAttackAnimation
import com.example.cardgame.ui.components.player.PlayerHand
import com.example.cardgame.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToMain: () -> Unit
) {
    // Player Properties
    val playerHand by viewModel.playerHandState
    val playerBoard by viewModel.playerBoardState
    val playerMana by viewModel.playerMana
    val playerMaxMana by viewModel.playerMaxMana
    val playerHealth by viewModel.playerHealth
    val selectedUnit by viewModel.selectedUnitPosition

    // Opponent Properties
    val opponentBoard by viewModel.opponentBoardState
    val opponentHealth by viewModel.opponentHealth

    // Game state
    val gameState by viewModel.gameState
    val isGameOver by viewModel.isGameOver
    val isPlayerWinner by viewModel.isPlayerWinner
    val isPlayerTurn by viewModel.isPlayerTurn
    val statusMessage by viewModel.statusMessage

    // Animation States
    val isSimpleAttackVisible by viewModel.isSimpleAttackVisible
    val attackingUnitType by viewModel.attackingUnitType
    val attackTargetPosition by viewModel.attackTargetPosition
    val isDamageNumberVisible by viewModel.isDamageNumberVisible
    val damageToShow by viewModel.damageToShow
    val damagePosition by viewModel.damagePosition
    val isHealingEffect by viewModel.isHealingEffect

    LaunchedEffect(key1 = Unit) {
        viewModel.startGame()
    }

    // Wrap everything in a Box to allow overlays
    Box(modifier = Modifier.fillMaxSize()) {
        // Main game content
        if (isGameOver) {
            GameOverScreen(
                isPlayerWinner = isPlayerWinner,
                onReturnToMainMenu = { onNavigateToMain() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Status message
                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Opponent's portrait
                PlayerPortrait(
                    playerName = "Opponent",
                    health = opponentHealth,
                    maxHealth = viewModel.playerMaxHealth.value,
                    isCurrentPlayer = !isPlayerTurn,
                    isTargetable = selectedUnit != -1,
                    onPortraitClick = { viewModel.attackOpponentDirectly() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .onGloballyPositioned { coordinates ->
                            // Register the center position of the opponent portrait
                            val bounds = coordinates.boundsInWindow()
                            val centerX = bounds.center.x
                            val centerY = bounds.center.y
                            viewModel.registerOpponentPortraitPosition(centerX, centerY)
                        }
                )

                // Opponent's board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    val activeOpponentFormations = viewModel.getOpponentActiveFormations()
                    BoardWithFormationTracking(
                        units = opponentBoard,
                        selectedUnit = selectedUnit,
                        isPlayerBoard = false,
                        onUnitClick = { position ->
                            if (selectedUnit != -1) {
                                viewModel.attackEnemyUnit(position)
                            }
                        },
                        registerPositions = { index, x, y ->
                            viewModel.registerSlotPosition(1, index, x, y)
                        },
                        activeFormations = activeOpponentFormations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(4.dp)
                    )
                }

                GameStatusBar(
                    playerMana = playerMana,
                    playerMaxMana = playerMaxMana,
                    isPlayerTurn = isPlayerTurn,
                    onEndTurn = { viewModel.endTurn() }
                )

                // Player's board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    val activePlayerFormations = viewModel.getPlayerActiveFormations()
                    BoardWithFormationTracking(
                        units = playerBoard,
                        selectedUnit = selectedUnit,
                        isPlayerBoard = true,
                        onUnitClick = { position -> viewModel.selectUnitForAttack(position) },
                        registerPositions = { index, x, y ->
                            viewModel.registerSlotPosition(0, index, x, y)
                        },
                        activeFormations = activePlayerFormations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player's hand
                PlayerHand(
                    cards = playerHand,
                    playerMana = playerMana,
                    onCardClick = { index -> viewModel.playCard(index) }
                )

                // Player's portrait
                PlayerPortrait(
                    playerName = "Player",
                    health = playerHealth,
                    maxHealth = viewModel.playerMaxHealth.value,
                    isCurrentPlayer = isPlayerTurn,
                    isTargetable = false,
                    onPortraitClick = { /* No action needed */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .onGloballyPositioned { coordinates ->
                            // Register the center position of the player portrait
                            val bounds = coordinates.boundsInWindow()
                            val centerX = bounds.center.x
                            val centerY = bounds.center.y
                            viewModel.registerPlayerPortraitPosition(centerX, centerY)
                        }
                )
            }
        }

        // Animation layers - these are drawn on top of everything else
        // Place them inside the main Box but outside the Column and GameOverScreen
        // Use zIndex to ensure they appear on top

        // Attack animation
        if (isSimpleAttackVisible) {
            SimpleAttackAnimation(
                isVisible = isSimpleAttackVisible,
                unitType = attackingUnitType,
                targetX = attackTargetPosition.first,
                targetY = attackTargetPosition.second,
                onAnimationComplete = { /* Animation will be handled by ViewModel */ },
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
    }
}

