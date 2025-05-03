package com.example.cardgame.ui.screens

import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val availableDeckNames by viewModel.availableDeckNames
    val availableDeckIds by viewModel.availableDecks
    val selectedPlayerDeck by viewModel.selectedPlayerDeck
    val selectedOpponentDeck by viewModel.selectedOpponentDeck

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
                deckNames = availableDeckNames,
                deckIds = availableDeckIds,
                selectedDeck = selectedPlayerDeck,
                onDeckSelected = { viewModel.setPlayerDeck(it) },
                modifier = Modifier.weight(1f),
                highlightColor = Color(0xFF5271FF)
            )

            // Opponent deck selection
            DeckSelectionColumn(
                title = "OPPONENT",
                deckNames = availableDeckNames,
                deckIds = availableDeckIds,
                selectedDeck = selectedOpponentDeck,
                onDeckSelected = { viewModel.setOpponentDeck(it) },
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
    deckNames: List<String>,
    deckIds: List<String>,
    selectedDeck: String?,
    onDeckSelected: (String) -> Unit,
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


        Log.d("SelectedDeck",selectedDeck.toString())

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
            itemsIndexed(deckIds) { index, deckId ->
                val deckTitleName = deckNames.getOrNull(index) ?: "Default Deck Name"
                DeckListItem(
                    deckName = deckTitleName,
                    isSelected = deckId == selectedDeck,
                    onClick = { onDeckSelected(deckId) },
                    highlightColor = highlightColor
                )
            }
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