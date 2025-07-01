package io.github.nihalhorseless.eternalglory.util

import io.github.nihalhorseless.eternalglory.data.model.campaign.BoardSetup
import io.github.nihalhorseless.eternalglory.data.model.campaign.Campaign
import io.github.nihalhorseless.eternalglory.data.model.campaign.CampaignLevel
import io.github.nihalhorseless.eternalglory.data.model.campaign.Difficulty
import io.github.nihalhorseless.eternalglory.data.model.campaign.SpecialRule

object CampaignData {
    val napoleonCampaign = Campaign(
        id = "napoleon_campaign",
        name = "Napoleon's Conquest",
        description = "Battle against Napoleon's finest marshals and ultimately face the Emperor himself!",
        levels = listOf(
            CampaignLevel(
                id = "level_1",
                name = "Marshal Murat's Cavalry",
                description = "Face Joachim Murat, Napoleon's 'Bravest of the Brave' and cavalry commander.",
                opponentName = "Marshal Murat",
                opponentDeckId = "murat_deck",  // A deck heavy on cavalry units
                difficulty = Difficulty.EASY,
                specialRules = listOf(
                    SpecialRule.StartingBoard(listOf(
                        BoardSetup(unitId = 101, row = 1, col = 1, isPlayerUnit = false),  // Enemy cavalry
                        BoardSetup(unitId = 102, row = 1, col = 3, isPlayerUnit = false)   // Enemy cavalry
                    ))
                ),
                reward = "infantry_reinforcement_card"
            ),

            CampaignLevel(
                id = "level_2",
                name = "Marshal Davout's Defense",
                description = "Challenge Louis-Nicolas Davout, Napoleon's most talented marshal and defensive genius.",
                opponentName = "Marshal Davout",
                opponentDeckId = "davout_deck.json",  // A defense-focused deck with fortifications
                difficulty = Difficulty.MEDIUM,
                specialRules = listOf(
                    SpecialRule.StartingBoard(listOf(
                        BoardSetup(unitId = 201, row = 1, col = 2, isPlayerUnit = false),  // Enemy wall
                        BoardSetup(unitId = 202, row = 1, col = 1, isPlayerUnit = false)   // Enemy tower
                    )),
                    SpecialRule.AdditionalCards(listOf(303, 304))  // Give player some siege cards
                ),
                reward = "artillery_card"
            ),

            CampaignLevel(
                id = "level_3",
                name = "Marshal Ney's Assault",
                description = "Withstand Michel Ney's relentless frontal assault tactics.",
                opponentName = "Marshal Ney",
                opponentDeckId = "ney_deck.json",  // Aggressive, infantry-heavy deck
                difficulty = Difficulty.MEDIUM,
                specialRules = listOf(
                    SpecialRule.StartingBoard(listOf(
                        BoardSetup(unitId = 301, row = 2, col = 0, isPlayerUnit = false),  // Enemy infantry
                        BoardSetup(unitId = 302, row = 2, col = 1, isPlayerUnit = false),  // Enemy infantry
                        BoardSetup(unitId = 303, row = 2, col = 2, isPlayerUnit = false),  // Enemy infantry
                        BoardSetup(unitId = 401, row = 4, col = 2, isPlayerUnit = true)    // Player's fortification
                    )),
                    SpecialRule.ModifiedMana(1)  // Player gets +1 max mana
                ),
                reward = "taunt_tactic_card"
            ),

            CampaignLevel(
                id = "level_4",
                name = "Marshal Masséna's Tactics",
                description = "Outmaneuver André Masséna, the 'Child of Victory' known for his tactical brilliance.",
                opponentName = "Marshal Masséna",
                opponentDeckId = "massena_deck",  // Balanced deck with many tactical cards
                difficulty = Difficulty.HARD,
                specialRules = listOf(
                    SpecialRule.CustomObjective(
                        description = "Win the battle while keeping at least 20 health",
                        checkCompletion = { gameManager ->
                            gameManager.players[0].health >= 20 &&
                                    gameManager.players[1].health <= 0
                        }
                    )
                ),
                reward = "special_tactic_cards"
            ),

            CampaignLevel(
                id = "level_5",
                name = "Emperor Napoleon Bonaparte",
                description = "Face the Emperor himself in the ultimate battle of wits and strategy!",
                opponentName = "Napoleon Bonaparte",
                opponentDeckId = "napoleon_deck.json",  // A powerful deck with unique cards
                difficulty = Difficulty.LEGENDARY,
                specialRules = listOf(
                    SpecialRule.ModifiedMana(2),  // Player gets +2 max mana to have a chance
                    SpecialRule.StartingBoard(listOf(
                        // Napoleon's Imperial Guard
                        BoardSetup(unitId = 501, row = 1, col = 1, isPlayerUnit = false),  // Old Guard infantry
                        BoardSetup(unitId = 502, row = 1, col = 3, isPlayerUnit = false),  // Imperial Guard cavalry
                        BoardSetup(unitId = 503, row = 0, col = 2, isPlayerUnit = false)   // Guard artillery
                    ))
                ),
                reward = "napoleon_strategy_deck"  // A special deck as final reward
            )
        )
    )
}