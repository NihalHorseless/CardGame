package com.example.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.ui.theme.scallopedCircleShape
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
    val selectedDeckInfo by viewModel.currentDeckInfo

    // State to track which player's deck we're currently selecting
    var currentSelectedPlayer by remember { mutableIntStateOf(0) } // 0 for Player 1, 1 for Player 2

    // State for current deck being browsed for each player
    var player1DeckIndex by remember { mutableIntStateOf(0) }
    var player2DeckIndex by remember { mutableIntStateOf(0) }

    // Get max mana from ViewModel
    val maxMana by viewModel.maxMana
    val maxManaOptions = listOf(10, 12, 15)

    LaunchedEffect(Unit) {
        viewModel.loadAvailableDecks()
    }

    // Get current deck index based on selected player
    val currentDeckIndex = if (currentSelectedPlayer == 0) player1DeckIndex else player2DeckIndex
    val currentDeck = availableDeckNames.getOrNull(currentDeckIndex) ?: "Player Deck"
    val currentDeckId = availableDeckIds.getOrNull(currentDeckIndex) ?: "player_deck"

    LaunchedEffect(currentDeckId) {
        viewModel.loadDeckInfo(deckId = currentDeckId)
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
        // Title
        Text(
            text = "DECK SELECTION",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Player selection filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val playerColor = Color(0xFF5271FF)
            val opponentColor = Color(0xFFFF5252)

            // Player 1 chip
            FilterChip(
                selected = currentSelectedPlayer == 0,
                onClick = {
                    currentSelectedPlayer = 0
                    viewModel.playMenuScrollSound()
                },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.eagle_standard),
                            tint = if (currentSelectedPlayer == 0) Color.White else Color.Gray,
                            modifier = Modifier
                                .size(32.dp)
                                .fillMaxWidth(),
                            contentDescription = "Deck Select PLayer Icon"
                        )

                        Text(
                            "You",
                            textAlign = TextAlign.Center,
                            color = if (currentSelectedPlayer == 0) Color.White else Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                },
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
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(80.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Player 2 chip
            FilterChip(
                selected = currentSelectedPlayer == 1,
                onClick = {
                    currentSelectedPlayer = 1
                    viewModel.playMenuScrollSound()
                },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.eagle_standard),
                            tint = if (currentSelectedPlayer == 1) Color.White else Color.Gray,
                            modifier = Modifier
                                .size(32.dp)
                                .fillMaxWidth(),
                            contentDescription = "Deck Select Bot Icon"
                        )

                        Text(
                            "Bot",
                            textAlign = TextAlign.Center,
                            color = if (currentSelectedPlayer == 1) Color.White else Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }

                },
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
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Deck browser section
        if (availableDeckNames.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left arrow
                IconButton(
                    onClick = {
                        if (currentSelectedPlayer == 0) {
                            if (player1DeckIndex > 0) {
                                player1DeckIndex--
                                viewModel.playMenuScrollSound()
                            }
                        } else {
                            if (player2DeckIndex > 0) {
                                player2DeckIndex--
                                viewModel.playMenuScrollSound()
                            }
                        }
                    },
                    enabled = currentDeckIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Deck",
                        tint = if (currentDeckIndex > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Deck display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.8f)
                        .background(
                            color = Color(0xFF343861),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 3.dp,
                            color = if (currentSelectedPlayer == 0) Color(0xFF5271FF) else Color(
                                0xFFFF5252
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            currentDeckId.let {
                                if (currentSelectedPlayer == 0) {
                                    viewModel.setPlayerDeck(it)
                                } else {
                                    viewModel.setOpponentDeck(it)
                                }
                                viewModel.playMenuSoundOne()
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Deck icon
                        Image(
                            painter = painterResource(R.drawable.card_back),
                            contentDescription = "Deck Icon",
                            modifier = Modifier.size(120.dp)
                        )

                        // Deck name
                        Text(
                            text = currentDeck,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Selection status
                        val isSelected = if (currentSelectedPlayer == 0) {
                            currentDeckId == selectedPlayerDeck
                        } else {
                            currentDeckId == selectedOpponentDeck
                        }

                        if (isSelected) {
                            Text(
                                text = "✓ Selected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        } else {
                            Text(
                                text = "Tap to select",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        // Deck stats (placeholder)
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!selectedDeckInfo.isNullOrEmpty()) {
                                DeckStatItem(
                                    "Units ${selectedDeckInfo!!["units"]}",
                                    iconRes = R.drawable.rifle_with_bayonet
                                )
                                DeckStatItem(
                                    "Forts ${selectedDeckInfo!!["forts"]}",
                                    iconRes = R.drawable.fortification_tower
                                )
                                DeckStatItem(
                                    "Tactics ${selectedDeckInfo!!["tactics"]}",
                                    iconRes = R.drawable.marshal_baton
                                )
                            }
                        }
                    }
                }

                // Right arrow
                IconButton(
                    onClick = {
                        if (currentSelectedPlayer == 0) {
                            if (player1DeckIndex < availableDeckNames.size - 1) {
                                player1DeckIndex++
                                viewModel.playMenuScrollSound()
                            }
                        } else {
                            if (player2DeckIndex < availableDeckNames.size - 1) {
                                player2DeckIndex++
                                viewModel.playMenuScrollSound()
                            }
                        }
                    },
                    enabled = currentDeckIndex < availableDeckNames.size - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Deck",
                        tint = if (currentDeckIndex < availableDeckNames.size - 1) Color.White else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Max Mana Selection
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Max Mana",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                maxManaOptions.forEach { manaOption ->
                    ManaOptionChip(
                        manaValue = manaOption,
                        isSelected = maxMana == manaOption,
                        onClick = {
                            viewModel.setMaxMana(manaOption)
                            viewModel.playMenuSoundOne()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start button
        Button(
            onClick = {
                viewModel.startGame()
                onStartGame()
            },
            enabled = selectedPlayerDeck != null && selectedOpponentDeck != null,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
                .border(
                    width = 4.dp,
                    color = Color(0xFF0D2E3E),
                    shape = RoundedCornerShape(2.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5271FF).copy(0.8f),
                disabledContainerColor = Color(0xFF5271FF).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(2.dp)
        ) {
            Text(
                text = "START GAME",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DeckStatItem(text: String, iconRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        Icon(contentDescription = "Deck Stat Item Image",
            painter = painterResource(iconRes),
            modifier = Modifier.size(32.dp).fillMaxWidth(),
            tint = Color.Gray.copy(0.8f))

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier
        )
    }
}

@Composable
fun ManaOptionChip(
    manaValue: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF5271FF) else Color(0xFF343861)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF5271FF) else Color(0xFF4A4A4A),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = manaValue.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}