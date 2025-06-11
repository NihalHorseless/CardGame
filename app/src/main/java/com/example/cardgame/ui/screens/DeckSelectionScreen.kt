package com.example.cardgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.ui.theme.bloodDropShape
import com.example.cardgame.ui.theme.kiteShieldShape
import com.example.cardgame.ui.theme.scallopedCircleShape
import com.example.cardgame.ui.theme.slenderSwordShape
import com.example.cardgame.ui.theme.thickSwordShape
import com.example.cardgame.ui.viewmodel.GameViewModel

@Composable
fun DeckSelectionScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit
) {
    val availableDeckNames by viewModel.availableDeckNames
    val availableDeckIds by viewModel.availableDecks
    val selectedPlayerDeck by viewModel.selectedPlayerDeck
    val selectedOpponentDeck by viewModel.selectedOpponentDeck

    // State to track which player's deck we're currently selecting
    var currentSelectedPlayer by remember { mutableStateOf(0) } // 0 for Player 1, 1 for Player 2

    LaunchedEffect(Unit) {
        viewModel.loadAvailableDecks()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2233),
                        Color(0xFF2D3250)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DECK SELECTION",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )


        // Deck grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableDeckIds.zip(availableDeckNames)) { (deckId, deckName) ->
                DeckGridItem(
                    deckName = deckName,
                    isPlayerSelected = deckId == selectedPlayerDeck,
                    isOpponentSelected = deckId == selectedOpponentDeck,
                    onClick = {
                        if (currentSelectedPlayer == 0) {
                            viewModel.setPlayerDeck(deckId)
                        } else {
                            viewModel.setOpponentDeck(deckId)
                        }
                    }
                )
            }
        }
        // Player selection filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val playerColor = Color(0xFF5271FF)
            val opponentColor = Color(0xFFFF5252)

            // Player 1 chip
            FilterChip(
                selected = currentSelectedPlayer == 0,
                onClick = { currentSelectedPlayer = 0 },
                label = { Text("Player 1", textAlign = TextAlign.Center) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF2A2D42),
                    labelColor = Color.White,
                    selectedContainerColor = playerColor,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = playerColor,
                    selectedBorderColor = playerColor,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 2.dp,
                    selected = currentSelectedPlayer == 0,
                    enabled = true
                ),
                shape = scallopedCircleShape,
                modifier = Modifier.padding(end = 8.dp)
                    .size(80.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Player 2 chip
            FilterChip(
                selected = currentSelectedPlayer == 1,
                onClick = { currentSelectedPlayer = 1 },
                label = { Text("Player 2", textAlign = TextAlign.Center) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF2A2D42),
                    labelColor = Color.White,
                    selectedContainerColor = opponentColor,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = opponentColor,
                    selectedBorderColor = opponentColor,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 2.dp,
                    selected = currentSelectedPlayer == 1,
                    enabled = true
                ),
                shape = scallopedCircleShape,
                modifier = Modifier.padding(end = 8.dp)
                    .size(80.dp)
            )
        }
        Spacer(modifier = Modifier.size(48.dp))

        // Start button
        Button(
            onClick = {
                viewModel.startGame()
                onStartGame()
            },
            enabled = selectedPlayerDeck != null && selectedOpponentDeck != null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(56.dp)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5271FF),
                disabledContainerColor = Color(0xFF5271FF).copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "START GAME",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DeckGridItem(
    deckName: String,
    isPlayerSelected: Boolean,
    isOpponentSelected: Boolean,
    onClick: () -> Unit
) {
    val playerHighlightColor = Color(0xFF5271FF)
    val opponentHighlightColor = Color(0xFFFF5252)

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2D42))
            .border(
                width = 2.dp,
                color = when {
                    isPlayerSelected && isOpponentSelected -> Color(0xFFFFD700) // Gold when both selected
                    isPlayerSelected -> playerHighlightColor
                    isOpponentSelected -> opponentHighlightColor
                    else -> Color(0xFF3D4160)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = deckName,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Player indicators
            if (isPlayerSelected || isOpponentSelected) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPlayerSelected) {
                        PlayerTag("P1", playerHighlightColor)
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    if (isOpponentSelected) {
                        PlayerTag("P2", opponentHighlightColor)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, slenderSwordShape)
            .border(0.5.dp, Color.White.copy(alpha = 0.5f), slenderSwordShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}