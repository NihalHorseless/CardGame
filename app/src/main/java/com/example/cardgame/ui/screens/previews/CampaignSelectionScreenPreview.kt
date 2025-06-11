package com.example.cardgame.ui.screens.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.ui.screens.CampaignSelectionScreen
import com.example.cardgame.ui.theme.CardGameTheme

@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun CampaignSelectionScreenPreview() {
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
            // Create mock campaigns for the preview
            val mockCampaigns = listOf(
                Campaign(
                    id = "napoleon_campaign",
                    name = "Napoleon's Conquest",
                    description = "Battle against Napoleon's finest marshals and ultimately face the Emperor himself!",
                    levels = listOf(
                        CampaignLevel(
                            id = "level_1",
                            name = "The Prince without a Crown",
                            description = "Face Marshal Ponitowski, the Polish prince and cavalry commander.",
                            opponentName = "Marshal Ponitowski",
                            opponentDeckId = "ponitowski_deck",
                            difficulty = Difficulty.EASY,
                            isCompleted = true
                        ),
                        CampaignLevel(
                            id = "level_2",
                            name = "The Brutus of Napoleon",
                            description = "Challenge Marshal Marmont, Napoleon's former friend turned betrayer.",
                            opponentName = "Marshal Marmont",
                            opponentDeckId = "marmont_deck",
                            difficulty = Difficulty.MEDIUM,
                            isCompleted = false
                        ),
                        CampaignLevel(
                            id = "level_3",
                            name = "Bras de Fer",
                            description = "Battle with Marshal Soult, Napoleon's 'Arm of Iron'.",
                            opponentName = "Marshal Soult",
                            opponentDeckId = "soult_deck",
                            difficulty = Difficulty.MEDIUM,
                            isCompleted = false
                        ),
                        CampaignLevel(
                            id = "level_4",
                            name = "The Bravest of the Brave",
                            description = "Face Marshal Ney, known for his courage on the battlefield.",
                            opponentName = "Marshal Ney",
                            opponentDeckId = "ney_deck",
                            difficulty = Difficulty.HARD,
                            isCompleted = false
                        ),
                        CampaignLevel(
                            id = "level_5",
                            name = "Vive L'Empereur",
                            description = "The final confrontation with Emperor Napoleon himself!",
                            opponentName = "Napoleon Bonaparte",
                            opponentDeckId = "napoleon_deck",
                            difficulty = Difficulty.LEGENDARY,
                            isCompleted = false
                        )
                    )
                ),
                Campaign(
                    id = "russian_campaign",
                    name = "Russian Winter",
                    description = "Experience Napoleon's disastrous invasion of Russia and the perilous retreat.",
                    levels = listOf(
                        CampaignLevel(
                            id = "russia_level_1",
                            name = "Crossing the Niemen",
                            description = "The Grande Armée begins its invasion of Russia.",
                            opponentName = "General Barclay",
                            opponentDeckId = "barclay_deck",
                            difficulty = Difficulty.MEDIUM,
                            isCompleted = false
                        )
                    )
                )
            )

            CampaignSelectionScreen(
                campaigns = mockCampaigns,
                onCampaignSelected = {},
                onBackPressed = {}
            )
        }
    }
}

@Preview(name = "Pixel 2", device = "id:pixel_2")
@Composable
fun CampaignSelectionScreenPreviewPixel2() {
    CampaignSelectionScreenPreview()
}

@Preview(name = "Pixel 3", device = "id:pixel_3")
@Composable
fun CampaignSelectionScreenPreviewPixel3() {
    CampaignSelectionScreenPreview()
}

@Preview(name = "Pixel 4", device = "id:pixel_4")
@Composable
fun CampaignSelectionScreenPreviewPixel4() {
    CampaignSelectionScreenPreview()
}

@Preview(name = "Pixel 5", device = "id:pixel_5")
@Composable
fun CampaignSelectionScreenPreviewPixel5() {
    CampaignSelectionScreenPreview()
}