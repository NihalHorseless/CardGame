package com.example.cardgame.ui.screens.previews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.cardgame.R
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.ui.screens.LevelSelectionScreen
import com.example.cardgame.ui.theme.CardGameTheme

@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun LevelSelectionScreenPreview() {
    CardGameTheme {
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
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.campaign_menu_background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // Create mock campaign data for the preview
            val mockCampaign = Campaign(
                id = "napoleon_campaign",
                name = "Napoleon's Conquest",
                description = "Battle against Napoleon's finest marshals and ultimately face the Emperor himself!",
                levels = listOf(
                    CampaignLevel(
                        id = "level_1",
                        name = "The Prince without a Crown",
                        description = "Józef Antoni Poniatowski was born in Vienna 7 May 1763, " +
                                "he was the nephew of the last remaining king of Poland. " +
                                "Despite his Polish heritage, Poniatowski was raised in Vienna where he pursued a military career " +
                                "He had fought for the Austrian army for the war against the Ottomans \n" +
                                "His uncle called him for his services for the war against the Russians which saw the partition of Poland ending the dynasty. " +
                                "Eventually he was exiled which led their paths cross with Napoleon, at last he was promoted to marshal before he died in the battle of Leipzig ",
                        opponentName = "Marshal Ponitowski",
                        opponentDeckId = "ponitowski_deck",
                        difficulty = Difficulty.EASY,
                        isCompleted = true,
                        specialRules = listOf(),
                        reward = "infantry_reinforcement_card"
                    ),
                    CampaignLevel(
                        id = "level_2",
                        name = "The Brutus of Napoleon",
                        description = "Auguste de Marmont was born in Châtillon-sur-Seine 20 July 1774. " +
                                "He was the son of an ex-officer in the army who belonged to the petite noblesse. " +
                                "Just like his father he pursued a military career with the supervision of him in Dijon where he met Napoleon...",
                        opponentName = "Marshal Marmont",
                        opponentDeckId = "marmont_deck",
                        difficulty = Difficulty.MEDIUM,
                        isCompleted = false,
                        specialRules = listOf(),
                        reward = "artillery_card"
                    ),
                    CampaignLevel(
                        id = "level_3",
                        name = "Bras de Fer",
                        description = "Jean-de-Dieu Soult was born in Saint-Amans-la-Bastide 29 March 1769. " +
                                "Son of a country notary, Soult wanted to pursue a career in Law but the early passing of his father made him enlist in the Royal Academy...",
                        opponentName = "Marshal Soult",
                        opponentDeckId = "soult_deck",
                        difficulty = Difficulty.MEDIUM,
                        isCompleted = false,
                        specialRules = listOf(),
                        reward = "taunt_tactic_card"
                    ),
                    CampaignLevel(
                        id = "level_4",
                        name = "The Bravest of the Brave",
                        description = "Michel Ney was born in the small town of Sarrelouis 10 January 1769. " +
                                "Son of a retired veteran, Ney had occupied himself with various civil duties...",
                        opponentName = "Marshal Ney",
                        opponentDeckId = "ney_deck",
                        difficulty = Difficulty.HARD,
                        isCompleted = false,
                        specialRules = listOf(),
                        reward = "special_tactic_cards"
                    ),
                    CampaignLevel(
                        id = "level_5",
                        name = "Vive L'Empereur",
                        description = "Napoleon Bonaparte was born in the island of Corsica on 15 August 1769 as the son of a minor nobility in the Corsican aristocracy...",
                        opponentName = "Napoleon Bonaparte",
                        opponentDeckId = "napoleon_deck",
                        difficulty = Difficulty.LEGENDARY,
                        isCompleted = false,
                        specialRules = listOf(),
                        reward = "napoleon_strategy_deck"
                    )
                )
            )

            // Mock deck data
            val mockPlayerDeckNames = listOf(
                "French Forces",
                "Grande Armée",
                "Napoleon's Guard",
                "Imperial Artillery",
                "Light Cavalry Division"
            )

            val mockPlayerDeckIds = listOf(
                "french_forces_deck",
                "grande_armee_deck",
                "napoleons_guard_deck",
                "imperial_artillery_deck",
                "light_cavalry_division_deck"
            )

            LevelSelectionScreen(
                campaign = mockCampaign,
                playerDecks = mockPlayerDeckIds,
                playerDeckNames = mockPlayerDeckNames,
                selectedDeck = "grande_armee_deck",
                onDeckSelected = {},
                onLevelScroll = {},
                onLevelSelected = {},
                onBackPressed = {},
                onInitial = {},
                onLeaveGame = {}
            )
        }
    }
}

@Preview(name = "Pixel 2", device = "id:pixel_2")
@Composable
fun LevelSelectionScreenPreviewPixel2() {
    LevelSelectionScreenPreview()
}

@Preview(name = "Pixel 3", device = "id:pixel_3")
@Composable
fun LevelSelectionScreenPreviewPixel3() {
    LevelSelectionScreenPreview()
}

@Preview(name = "Pixel 4", device = "id:pixel_4")
@Composable
fun LevelSelectionScreenPreviewPixel4() {
    LevelSelectionScreenPreview()
}

@Preview(name = "Pixel 5", device = "id:pixel_5")
@Composable
fun LevelSelectionScreenPreviewPixel5() {
    LevelSelectionScreenPreview()
}