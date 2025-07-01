@file:Suppress("EmptyMethod", "EmptyMethod", "EmptyMethod")

package io.github.nihalhorseless.eternalglory.ui.screens.previews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nihalhorseless.eternalglory.R
import io.github.nihalhorseless.eternalglory.data.enum.FortificationType
import io.github.nihalhorseless.eternalglory.data.enum.UnitEra
import io.github.nihalhorseless.eternalglory.data.enum.UnitType
import io.github.nihalhorseless.eternalglory.data.model.card.Card
import io.github.nihalhorseless.eternalglory.data.model.card.FortificationCard
import io.github.nihalhorseless.eternalglory.data.model.card.UnitCard
import io.github.nihalhorseless.eternalglory.ui.screens.CompactCardItem
import io.github.nihalhorseless.eternalglory.ui.screens.DeckCards
import io.github.nihalhorseless.eternalglory.ui.screens.SearchBar
import io.github.nihalhorseless.eternalglory.ui.theme.libreFont

@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun DeckEditorScreenPreview() {
    // Create a minimal mock for preview
    val mockViewModel = object {
        @SuppressLint("UnrememberedMutableState")
        val filteredCards = mutableStateOf(createMockCards())
        @SuppressLint("UnrememberedMutableState")
        val currentDeckCards = mutableStateListOf<Card>()
        @SuppressLint("UnrememberedMutableState")
        val searchQuery = mutableStateOf("")

        fun setSearchQuery(query: String) {}

        fun removeCardFromDeck(index: Int) {
            if (index < currentDeckCards.size) {
                currentDeckCards.removeAt(index)
            }
        }
        fun isCurrentDeckValid(): Boolean = true
    }

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
        Image(
            painter = painterResource(id = R.drawable.available_cards_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button, title, and save button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Search bar
                SearchBar(
                    query = mockViewModel.searchQuery.value,
                    onQueryChange = { mockViewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth(0.5f)
                )

                // Save button
                Button(
                    onClick = {},
                    enabled = mockViewModel.isCurrentDeckValid(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E6432),
                        disabledContainerColor = Color(0xFF422D1E).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .width(80.dp)
                        .border(
                            width = 4.dp,
                            color = Color(0xFF754311),
                            shape = RectangleShape
                        ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            // Card count indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Cards",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = libreFont,
                    color = Color(0xFF3A1E07)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            // Card browser with cards and pagination
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Left arrow
                IconButton({}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Page",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Card grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(mockViewModel.filteredCards.value.take(12)) { card ->
                        CompactCardItem(
                            card = card,
                            onClick = {},
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(0.7f)
                        )
                    }
                }

                // Right arrow
                IconButton({}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Page",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                // Current deck cards at bottom
                Text(
                    text = "Your Deck: ${mockViewModel.currentDeckCards.size}/30",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = libreFont,
                    color = Color(0xFF633E18)
                )
            }

            DeckCards(
                cards = mockViewModel.currentDeckCards,
                onCardClick = { _, index ->
                    mockViewModel.removeCardFromDeck(index)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
@Preview(name = "Pixel 2", device = "id:pixel_2")
@Composable
fun DeckEditorScreenPreviewPixel2() {
    DeckEditorScreenPreview()
}

@Preview(name = "Pixel 3", device = "id:pixel_3")
@Composable
fun DeckEditorScreenPreviewPixel3() {
    DeckEditorScreenPreview()
}

@Preview(name = "Pixel 4", device = "id:pixel_4")
@Composable
fun DeckEditorScreenPreviewPixel4() {
    DeckEditorScreenPreview()
}

@Preview(name = "Pixel 5", device = "id:pixel_5")
@Composable
fun DeckEditorScreenPreviewPixel5() {
    DeckEditorScreenPreview()
}

private fun createMockCards(): List<Card> {
    val mockCards = mutableListOf<Card>()

    // Add a few UnitCards
    mockCards.add(UnitCard(
        id = 1,
        name = "French Hussar",
        description = "Cavalry unit with charge",
        manaCost = 5,
        imagePath = "unit_medieval_knight",
        attack = 5,
        health = 5,
        maxHealth = 5,
        unitType = UnitType.CAVALRY,
        unitEra = UnitEra.NAPOLEONIC,
        abilities = mutableListOf(),
        hasCharge = true,
        hasTaunt = false
    ))

    mockCards.add(
        UnitCard(
        id = 2,
        name = "Line Infantry",
        description = "Basic infantry unit",
        manaCost = 3,
        imagePath = "unit_infantry",
        attack = 3,
        health = 4,
        maxHealth = 4,
        unitType = UnitType.INFANTRY,
        unitEra = UnitEra.NAPOLEONIC,
        abilities = mutableListOf(),
        hasCharge = false,
        hasTaunt = false
    )
    )

    // Add a FortificationCard
    mockCards.add(
        FortificationCard(
        id = 3,
        name = "Archer Tower",
        description = "Tower with ranged attack",
        manaCost = 3,
        imagePath = "fortification_archer_tower",
        attack = 2,
        health = 4,
        maxHealth = 4,
        fortType = FortificationType.TOWER,
        canAttackThisTurn = false
    )
    )
    // Add a few UnitCards
    mockCards.add(UnitCard(
        id = 4,
        name = "French Hussar",
        description = "Cavalry unit with charge",
        manaCost = 5,
        imagePath = "unit_medieval_knight",
        attack = 5,
        health = 5,
        maxHealth = 5,
        unitType = UnitType.CAVALRY,
        unitEra = UnitEra.NAPOLEONIC,
        abilities = mutableListOf(),
        hasCharge = true,
        hasTaunt = false
    ))

    mockCards.add(
        UnitCard(
            id = 5,
            name = "Line Infantry",
            description = "Basic infantry unit",
            manaCost = 3,
            imagePath = "unit_infantry",
            attack = 3,
            health = 4,
            maxHealth = 4,
            unitType = UnitType.INFANTRY,
            unitEra = UnitEra.NAPOLEONIC,
            abilities = mutableListOf(),
            hasCharge = false,
            hasTaunt = false
        )
    )

    // Add a FortificationCard
    mockCards.add(
        FortificationCard(
            id = 6,
            name = "Archer Tower",
            description = "Tower with ranged attack",
            manaCost = 3,
            imagePath = "fortification_archer_tower",
            attack = 2,
            health = 4,
            maxHealth = 4,
            fortType = FortificationType.TOWER,
            canAttackThisTurn = false
        )
    )
    // Add a few UnitCards
    mockCards.add(UnitCard(
        id = 7,
        name = "French Hussar",
        description = "Cavalry unit with charge",
        manaCost = 5,
        imagePath = "unit_medieval_knight",
        attack = 5,
        health = 5,
        maxHealth = 5,
        unitType = UnitType.CAVALRY,
        unitEra = UnitEra.NAPOLEONIC,
        abilities = mutableListOf(),
        hasCharge = true,
        hasTaunt = false
    ))

    mockCards.add(
        UnitCard(
            id = 8,
            name = "Line Infantry",
            description = "Basic infantry unit",
            manaCost = 3,
            imagePath = "unit_infantry",
            attack = 3,
            health = 4,
            maxHealth = 4,
            unitType = UnitType.INFANTRY,
            unitEra = UnitEra.NAPOLEONIC,
            abilities = mutableListOf(),
            hasCharge = false,
            hasTaunt = false
        )
    )

    // Add a FortificationCard
    mockCards.add(
        FortificationCard(
            id = 9,
            name = "Archer Tower",
            description = "Tower with ranged attack",
            manaCost = 3,
            imagePath = "fortification_archer_tower",
            attack = 2,
            health = 4,
            maxHealth = 4,
            fortType = FortificationType.TOWER,
            canAttackThisTurn = false
        )
    )
    // Add a few UnitCards
    mockCards.add(UnitCard(
        id = 10,
        name = "French Hussar",
        description = "Cavalry unit with charge",
        manaCost = 5,
        imagePath = "unit_medieval_knight",
        attack = 5,
        health = 5,
        maxHealth = 5,
        unitType = UnitType.CAVALRY,
        unitEra = UnitEra.NAPOLEONIC,
        abilities = mutableListOf(),
        hasCharge = true,
        hasTaunt = false
    ))

    mockCards.add(
        UnitCard(
            id = 11,
            name = "Line Infantry",
            description = "Basic infantry unit",
            manaCost = 3,
            imagePath = "unit_infantry",
            attack = 3,
            health = 4,
            maxHealth = 4,
            unitType = UnitType.INFANTRY,
            unitEra = UnitEra.NAPOLEONIC,
            abilities = mutableListOf(),
            hasCharge = false,
            hasTaunt = false
        )
    )

    // Add a FortificationCard
    mockCards.add(
        FortificationCard(
            id = 12,
            name = "Archer Tower",
            description = "Tower with ranged attack",
            manaCost = 3,
            imagePath = "fortification_archer_tower",
            attack = 2,
            health = 4,
            maxHealth = 4,
            fortType = FortificationType.TOWER,
            canAttackThisTurn = false
        )
    )

    // Add more cards as needed...
    return mockCards
}