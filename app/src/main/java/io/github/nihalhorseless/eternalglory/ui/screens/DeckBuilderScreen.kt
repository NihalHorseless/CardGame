package io.github.nihalhorseless.eternalglory.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nihalhorseless.eternalglory.R
import io.github.nihalhorseless.eternalglory.data.model.card.Deck
import io.github.nihalhorseless.eternalglory.ui.viewmodel.DeckBuilderViewModel

@Composable
fun DeckBuilderScreen(
    viewModel: DeckBuilderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String?) -> Unit // null for new deck, deckId for edit
) {
    val playerDecks by viewModel.playerDecks

    // Keep track of current deck index for browsing
    var currentDeckIndex by remember { mutableIntStateOf(0) }

    // Get current deck or null if no decks
    val currentDeck = playerDecks.getOrNull(currentDeckIndex)

    // Load decks when screen is composed
    LaunchedEffect(Unit) {
        viewModel.loadPlayerDecks()
    }

    // Background gradient
    Box(
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
    ) {
        // Header and back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_icon),
                    tint = Color.White
                )
            }

            Text(
                text = "DECK BUILDER",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Add new deck button
            IconButton(
                onClick = { onNavigateToEditor(null) }, // null means create new
                enabled = playerDecks.size < 5
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New Deck",
                    tint = if (playerDecks.size < 5) Color.White else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (playerDecks.isEmpty()) {
                // Show empty state
                EmptyDeckState(onCreateDeck = { onNavigateToEditor(null) })
            } else {
                // Deck browser with navigation arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left arrow
                    IconButton(
                        onClick = {
                            if (currentDeckIndex > 0) {
                                currentDeckIndex--
                                viewModel.playMenuScrollSound()
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
                    currentDeck?.let { deck ->
                        DeckDisplay(
                            deck = deck,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                    }

                    // Right arrow
                    IconButton(
                        onClick = {
                            if (currentDeckIndex < playerDecks.size - 1) {
                                currentDeckIndex++
                                viewModel.playMenuScrollSound()
                            }
                        },
                        enabled = currentDeckIndex < playerDecks.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Deck",
                            tint = if (currentDeckIndex < playerDecks.size - 1) Color.White else Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Action buttons
                currentDeck?.let { deck ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToEditor(deck.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 2.dp,
                                color =  Color(0xFF0D2E3E),
                                shape = RoundedCornerShape(2.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5271FF)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "Edit Deck",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.deleteDeck(deck.id)
                            // If this was the last deck, adjust index
                            if (currentDeckIndex >= playerDecks.size - 1 && currentDeckIndex > 0) {
                                currentDeckIndex--
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 2.dp,
                                color =  Color(0xFF0D2E3E),
                                shape = RoundedCornerShape(2.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "Delete Deck",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeckDisplay(
    deck: Deck,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck image/preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f) // Card-like aspect ratio
                .background(
                    color = Color(0xFF343861),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF5271FF),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Deck preview/image
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Placeholder icon for deck
                Image(painter = painterResource(R.drawable.deck_icon), contentDescription = "Deck Back", modifier = Modifier)

                Spacer(modifier = Modifier.height(16.dp))

                // Deck name
                Text(
                    text = deck.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Deck description
                Text(
                    text = deck.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card count
                Text(
                    text = "${deck.cards.size} Cards",
                    fontSize = 16.sp,
                    color = Color(0xFF5271FF),
                    fontWeight = FontWeight.Medium
                )

                // Card type breakdown
                val unitCount = deck.cards.count { card ->
                    card.toString().contains("UnitCard")
                }
                val tacticCount = deck.cards.count { card ->
                    card.toString().contains("TacticCard")
                }
                val fortCount = deck.cards.size - unitCount - tacticCount

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CardTypeChip(type = "Units", count = unitCount)
                    CardTypeChip(type = "Tactics", count = tacticCount)
                    CardTypeChip(type = "Forts", count = fortCount)
                }
            }
        }
    }
}

@Composable
fun CardTypeChip(type: String, count: Int) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF2D3250),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$type: $count",
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

@Composable
fun EmptyDeckState(onCreateDeck: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.marshal_baton),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Decks Available",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first custom deck to get started",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateDeck,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5271FF)
            ),
            modifier = Modifier
                .height(48.dp)
                .width(200.dp)
        ) {
            Text(
                text = "Create New Deck",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

