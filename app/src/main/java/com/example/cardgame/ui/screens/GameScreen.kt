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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cardgame.ui.components.board.BoardWithFormationTracking
import com.example.cardgame.ui.components.board.GameStatusBar
import com.example.cardgame.ui.components.board.PlayerPortrait
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

    LaunchedEffect(key1 = Unit) {
        viewModel.startGame()
    }
// Game over overlay
    if (isGameOver) {
        GameOverScreen(
            isPlayerWinner = isPlayerWinner,
            onReturnToMainMenu = { onNavigateToMain() }
        )
    }
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Game status
            GameStatusBar(
                playerHealth = playerHealth,
                opponentHealth = opponentHealth,
                playerMana = playerMana,
                playerMaxMana = playerMaxMana,
                isPlayerTurn = isPlayerTurn,
                onEndTurn = { viewModel.endTurn() }
            )

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

            // Opponent's portrait and board area

            // Opponent's portrait
            PlayerPortrait(
                playerName = "Opponent",
                health = opponentHealth,
                maxHealth = viewModel.playerMaxHealth.value,
                isCurrentPlayer = !isPlayerTurn,
                isTargetable = selectedUnit != -1, // Targetable when a unit is selected for attack
                onPortraitClick = {
                    // Attack the opponent directly
                    viewModel.attackOpponentDirectly()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
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
                    activeFormations = activeOpponentFormations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(4.dp)
                )
            }


            Spacer(modifier = Modifier.weight(1f))

            // Player's board and portrait area


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
                    activeFormations = activePlayerFormations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(4.dp)
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Player's hand
            Text("Your Hand", color = Color.White)
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
                isTargetable = false, // Player portrait is never a target
                onPortraitClick = { /* No action needed */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            )

        }
    }

}

