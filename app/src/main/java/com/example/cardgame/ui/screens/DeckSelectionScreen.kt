package com.example.cardgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.ui.viewmodel.GameViewModel

@Composable
fun DeckSelectionScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit
) {
    val availableDecks by viewModel.availableDecks
    val selectedPlayerDeck by viewModel.selectedPlayerDeck
    val selectedOpponentDeck by viewModel.selectedOpponentDeck

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
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Main selection area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player deck selection
            DeckSelectionColumn(
                title = "YOUR DECK",
                decks = availableDecks,
                selectedDeck = selectedPlayerDeck,
                onDeckSelected = { viewModel.setPlayerDeck(it) },
                getDeckInfo = { viewModel.getDeckInfo(it) },
                modifier = Modifier.weight(1f),
                highlightColor = Color(0xFF5271FF)
            )

            // Opponent deck selection
            DeckSelectionColumn(
                title = "OPPONENT",
                decks = availableDecks,
                selectedDeck = selectedOpponentDeck,
                onDeckSelected = { viewModel.setOpponentDeck(it) },
                getDeckInfo = { viewModel.getDeckInfo(it) },
                modifier = Modifier.weight(1f),
                highlightColor = Color(0xFFFF5252)
            )
        }

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
                .padding(vertical = 8.dp)
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
fun DeckSelectionColumn(
    title: String,
    decks: List<String>,
    selectedDeck: String?,
    onDeckSelected: (String) -> Unit,
    getDeckInfo: (String) -> Deck?,
    modifier: Modifier = Modifier,
    highlightColor: Color
) {
    Column(
        modifier = modifier
            .background(
                Color(0xFF1A1C2A),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF3D4160),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = highlightColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        HorizontalDivider(color = Color(0xFF3D4160))

        // Selected deck info
        selectedDeck?.let { deckName ->
            val deck = getDeckInfo(deckName)
            if (deck != null) {
                DeckInfoPanel(deck, highlightColor)
            }
        }

        Text(
            text = "Available Decks",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Deck list
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(decks) { deckName ->
                DeckListItem(
                    deckName = deckName,
                    isSelected = deckName == selectedDeck,
                    onClick = { onDeckSelected(deckName) },
                    highlightColor = highlightColor
                )
            }
        }
    }
}

@Composable
fun DeckInfoPanel(
    deck: Deck,
    highlightColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                Color(0xFF252842),
                RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = highlightColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = deck.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = deck.description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Text(
            text = "${deck.cards.size} Cards",
            fontSize = 14.sp,
            color = highlightColor,
            fontWeight = FontWeight.Medium
        )

        // Card type distribution
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val unitCards = deck.cards.count { it.toString().contains("UnitCard") }
            val tacticCards = deck.cards.size - unitCards

            Text(
                text = "Units: $unitCards",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = "Tactics: $tacticCards",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DeckListItem(
    deckName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    highlightColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) highlightColor.copy(alpha = 0.2f) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) highlightColor else Color(0xFF3D4160),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = deckName,
            fontSize = 16.sp,
            color = if (isSelected) highlightColor else Color.White
        )
    }
}