package io.github.nihalhorseless.eternalglory.ui.screens.previews

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.nihalhorseless.eternalglory.data.model.card.Card
import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.ui.components.board.GameBoard
import io.github.nihalhorseless.eternalglory.ui.components.board.GameStatusBar
import io.github.nihalhorseless.eternalglory.ui.components.board.PlayerPortrait
import io.github.nihalhorseless.eternalglory.ui.components.player.OpponentHand


@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun GameScreenPreview() {

    val mockViewModel = object {
        // Game state
        @SuppressLint("UnrememberedMutableState")
        val selectedCell = mutableStateOf<Pair<Int, Int>?>(null)
        @SuppressLint("UnrememberedMutableState")
        val playerMana = mutableIntStateOf(5)
        @SuppressLint("UnrememberedMutableState")
        val playerMaxMana = mutableIntStateOf(10)
        @SuppressLint("UnrememberedMutableState")
        val playerHealth = mutableIntStateOf(30)
        @SuppressLint("UnrememberedMutableState")
        val opponentHealth = mutableIntStateOf(30)
        @SuppressLint("UnrememberedMutableState")
        val opponentHandSize = mutableIntStateOf(5)
        @SuppressLint("UnrememberedMutableState")
        val isPlayerTurn = mutableStateOf(true)

        @SuppressLint("UnrememberedMutableState")
        val opponentName = mutableStateOf("Mock Opponent")
        @SuppressLint("UnrememberedMutableState")
        val playerVisualHealth = mutableStateOf<Int?>(null)
        @SuppressLint("UnrememberedMutableState")
        val opponentVisualHealth = mutableStateOf<Int?>(null)

        // Movement and attack highlighting
        @SuppressLint("UnrememberedMutableState")
        val validMoveDestinations = mutableStateOf<List<Pair<Int, Int>>>(listOf())
        @SuppressLint("UnrememberedMutableState")
        val validAttackTargets = mutableStateOf<List<Pair<Int, Int>>>(listOf())

        @SuppressLint("UnrememberedMutableState")
        val visualHealthMap = mutableStateOf<Map<Card, Int>>(emptyMap())


        // Deployment system states
        @SuppressLint("UnrememberedMutableState")
        val validDeploymentPositions = mutableStateOf<List<Pair<Int, Int>>>(listOf())

        // Mock GameManager with a 5x5 board
        val gameManager = GameManager()

        // Mock functions that would be called by the UI
        fun onCellClick(row: Int, col: Int) {}
        fun registerCellPosition(row: Int, col: Int, x: Float, y: Float) {}
        fun attachBayonet(row: Int, col: Int) {}
    }

    // Render the GameScreen with mock data
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status message would go here

            // Top row with portraits
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Opponent's portrait
                PlayerPortrait(
                    playerName = mockViewModel.opponentName.value,
                    health = mockViewModel.opponentHealth.intValue,
                    maxHealth = mockViewModel.opponentHealth.intValue,
                    visualHealth = mockViewModel.opponentVisualHealth.value,
                    isCurrentPlayer = !mockViewModel.isPlayerTurn.value,
                    isTargetable = false,
                    onPortraitClick = { }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Opponent's Hand
                OpponentHand(
                    cardCount = mockViewModel.opponentHandSize.intValue,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }

            // Unified game board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                GameBoard(
                    gameBoard = mockViewModel.gameManager.gameBoard,
                    gameManager = mockViewModel.gameManager,
                    selectedCell = mockViewModel.selectedCell.value,
                    currentPlayerId = 0, // Player's ID
                    validDeploymentPositions = mockViewModel.validDeploymentPositions.value,
                    validMoveDestinations = mockViewModel.validMoveDestinations.value,
                    validAttackTargets = mockViewModel.validAttackTargets.value,
                    onCellClick = { row, col ->
                        mockViewModel.onCellClick(row, col)
                    },
                    onAttachBayonet = { row, col ->
                        mockViewModel.attachBayonet(row, col)
                    },
                    registerCellPosition = { row, col, x, y ->
                        mockViewModel.registerCellPosition(row, col, x, y)
                    },
                    visualHealthMap = mockViewModel.visualHealthMap.value,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Game status (turn, mana)
            GameStatusBar(
                playerMana = mockViewModel.playerMana.intValue,
                playerMaxMana = mockViewModel.playerMaxMana.intValue,
                isPlayerTurn = mockViewModel.isPlayerTurn.value,
                onEndTurn = { }
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
                    health = mockViewModel.playerHealth.intValue,
                    maxHealth = 30,
                    visualHealth = mockViewModel.playerVisualHealth.value,
                    isCurrentPlayer = mockViewModel.isPlayerTurn.value,
                    isTargetable = false,
                    onPortraitClick = { }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Empty mock hand for preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .background(Color(0xFF2D3250).copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Preview(name = "Pixel 2", device = "id:pixel_2")
@Composable
fun GuideScreenPreviewPixel2() {
    GameScreenPreview()
}

@Preview(name = "Pixel 3", device = "id:pixel_3")
@Composable
fun GuideScreenPreviewPixel3() {
    GameScreenPreview()
}

@Preview(name = "Pixel 4", device = "id:pixel_4")
@Composable
fun GuideScreenPreviewPixel4() {
    GameScreenPreview()
}

@Preview(name = "Pixel 5", device = "id:pixel_5")
@Composable
fun GuideScreenPreviewPixel5() {
    GameScreenPreview()
}

