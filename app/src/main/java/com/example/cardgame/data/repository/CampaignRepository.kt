package com.example.cardgame.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.data.model.campaign.SpecialRule
import com.example.cardgame.data.model.campaign.BoardSetup
import com.google.gson.Gson

/**
 * Repository for accessing and managing campaign data
 */
class CampaignRepository(private val context: Context) {
    private val TAG = "CampaignRepository"
    private val PREFS_NAME = "campaign_prefs"
    private val COMPLETED_LEVELS_KEY = "completed_levels"

    private val gson = Gson()

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // In-memory storage for campaigns
    private val campaigns = mutableMapOf<String, Campaign>()

    init {
        // Initialize with the default Napoleon campaign
        val napoleonCampaign = createNapoleonCampaign()
        campaigns[napoleonCampaign.id] = napoleonCampaign

        // Load saved progress
        loadSavedProgress()
    }

    /**
     * Load saved progress for all campaigns
     */
    private fun loadSavedProgress() {
        campaigns.forEach { (campaignId, campaign) ->
            val completedLevels = getCompletedLevels(campaignId)
            if (completedLevels.isNotEmpty()) {
                val updatedLevels = campaign.levels.map { level ->
                    if (level.id in completedLevels) level.copy(isCompleted = true) else level
                }
                campaigns[campaignId] = campaign.copy(levels = updatedLevels)
            }
        }
    }

    /**
     * Get a campaign by its ID
     */
    fun getCampaign(campaignId: String): Campaign? {
        return campaigns[campaignId]
    }

    /**
     * Update a campaign in the repository
     */
    fun updateCampaign(campaign: Campaign) {
        campaigns[campaign.id] = campaign

        // Save completed levels to preferences
        val completedLevels = campaign.levels
            .filter { it.isCompleted }
            .map { it.id }

        saveCampaignProgress(campaign.id, completedLevels)
    }

    /**
     * Get all available campaigns
     */
    fun getAllCampaigns(): List<Campaign> {
        return campaigns.values.toList()
    }

    /**
     * Save campaign progress (completed levels)
     */
    private fun saveCampaignProgress(campaignId: String, completedLevels: List<String>) {
        try {
            val json = gson.toJson(completedLevels)
            prefs.edit().putString("${COMPLETED_LEVELS_KEY}_$campaignId", json).apply()
            Log.d(TAG, "Saved progress for campaign $campaignId: $completedLevels")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving campaign progress", e)
        }
    }

    /**
     * Get completed levels for a campaign
     */
    private fun getCompletedLevels(campaignId: String): List<String> {
        val json =
            prefs.getString("${COMPLETED_LEVELS_KEY}_$campaignId", null) ?: return emptyList()

        return try {
            gson.fromJson(json, Array<String>::class.java).toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading completed levels", e)
            emptyList()
        }
    }

    /**
     * Creates the Napoleon campaign
     */
    private fun createNapoleonCampaign(): Campaign {
        return Campaign(
            id = "napoleon_campaign",
            name = "Napoleon's Conquest",
            description = "Battle against Napoleon's finest marshals and ultimately face the Emperor himself!",
            levels = listOf(
                CampaignLevel(
                    id = "level_1",
                    name = "Marshal Poniatowski, the Prince who never became the King",
                    description = "Józef Antoni Poniatowski was born in Vienna 7 May 1763 " +
                            "He was the nephew of the last remaining king of Poland " +
                            "Despite his Polish heritage, he was raised in Vienna where he pursued a military career " +
                            "During his early years he had fought for the Austrian army gaining experience at the war against the Ottomans " +
                            "His uncle called him for his services for the war against the Russians which saw the partition of Poland ending dynasty " +
                            "Eventually he was exiled which led their paths cross with Napoleon, since Napoleon was a supporter of independent Poland ",
                    opponentName = "Marshal Ponitowski",
                    opponentDeckId = "ponitowski_deck",  // A deck heavy on cavalry units
                    difficulty = Difficulty.EASY,
                    specialRules = listOf(
                        SpecialRule.StartingBoard(
                            listOf(
                                BoardSetup(
                                    unitId = 101,
                                    row = 1,
                                    col = 1,
                                    isPlayerUnit = false
                                ),  // Enemy cavalry
                                BoardSetup(
                                    unitId = 102,
                                    row = 1,
                                    col = 3,
                                    isPlayerUnit = false
                                )   // Enemy cavalry
                            )
                        )
                    ),
                    reward = "infantry_reinforcement_card"
                ),

                CampaignLevel(
                    id = "level_2",
                    name = "Marshal Marmont, the Brutus of Napoleon",
                    description = "Auguste de Marmont was born in Châtillon-sur-Seine 20 July 1774 " +
                            "He was the son of a ex-officer in the army who belonged to the petite noblesse " +
                            "Just like his father he pursued a military career with the supervision of him in Dijon where he met Napoleon" +
                            "Their paths crossed again in the Siege of Toulon (1793) which led to their mutual friendship as he became Napoleon's aide-de-camp " +
                            "Marmont followed Napoleon for the upcoming campaigns of Italy,Egypt and Dalmatia where he proved himself many times leading to his promotion to marshall ",
                    opponentName = "Marshal Marmont",
                    opponentDeckId = "marmont_deck",  // A defense-focused deck with fortifications
                    difficulty = Difficulty.MEDIUM,
                    specialRules = listOf(
                        SpecialRule.StartingBoard(
                            listOf(
                                BoardSetup(
                                    unitId = 201,
                                    row = 1,
                                    col = 2,
                                    isPlayerUnit = false
                                ),  // Enemy wall
                                BoardSetup(
                                    unitId = 202,
                                    row = 1,
                                    col = 1,
                                    isPlayerUnit = false
                                )   // Enemy tower
                            )
                        ),
                        SpecialRule.AdditionalCards(
                            listOf(
                                303,
                                304
                            )
                        )  // Give player some siege cards
                    ),
                    reward = "artillery_card"
                ),
                CampaignLevel(
                    id = "level_3",
                    name = "Catching Marshal Soult",
                    description = "Try to out fox the brilliant Jean-de-Dieu Soult.",
                    opponentName = "Marshal Soult",
                    opponentDeckId = "soult_deck",  // Aggressive, infantry-heavy deck
                    difficulty = Difficulty.MEDIUM,
                    specialRules = listOf(
                        SpecialRule.StartingBoard(
                            listOf(
                                BoardSetup(
                                    unitId = 301,
                                    row = 2,
                                    col = 0,
                                    isPlayerUnit = false
                                ),  // Enemy infantry
                                BoardSetup(
                                    unitId = 302,
                                    row = 2,
                                    col = 1,
                                    isPlayerUnit = false
                                ),  // Enemy infantry
                                BoardSetup(
                                    unitId = 303,
                                    row = 2,
                                    col = 2,
                                    isPlayerUnit = false
                                ),  // Enemy infantry
                                BoardSetup(
                                    unitId = 401,
                                    row = 4,
                                    col = 2,
                                    isPlayerUnit = true
                                )    // Player's fortification
                            )
                        ),
                        SpecialRule.ModifiedMana(1)  // Player gets +1 max mana
                    ),
                    reward = "taunt_tactic_card"
                ),

                CampaignLevel(
                    id = "level_4",
                    name = "Marshal Ney's Assault",
                    description = "Withstand Michel Ney's relentless frontal assault tactics.",
                    opponentName = "Marshal Ney",
                    opponentDeckId = "ney_deck",  // Aggressive, infantry-heavy deck
                    difficulty = Difficulty.MEDIUM,
                    specialRules = listOf(
                        SpecialRule.StartingBoard(
                            listOf(
                                BoardSetup(
                                    unitId = 301,
                                    row = 2,
                                    col = 0,
                                    isPlayerUnit = false
                                ),  // Enemy infantry
                                BoardSetup(
                                    unitId = 302,
                                    row = 2,
                                    col = 1,
                                    isPlayerUnit = false
                                ),  // Enemy infantry
                                BoardSetup(
                                    unitId = 303,
                                    row = 2,
                                    col = 2,
                                    isPlayerUnit = false
                                ),  // Enemy infantry
                                BoardSetup(
                                    unitId = 401,
                                    row = 4,
                                    col = 2,
                                    isPlayerUnit = true
                                )    // Player's fortification
                            )
                        ),
                        SpecialRule.ModifiedMana(1)  // Player gets +1 max mana
                    ),
                    reward = "taunt_tactic_card"
                ),

                CampaignLevel(
                    id = "level_5",
                    name = "Marshal Davout's Iron Will",
                    description = "Outmaster Louis-Nicolas Davout, the Prince of Eckmühl known for his tactical brilliance.",
                    opponentName = "Marshal Davout",
                    opponentDeckId = "davout_deck",  // Balanced deck with many tactical cards
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
                    id = "level_6",
                    name = "Lannes",
                    description = "Face Jean Lannes, a child of the revolution and dear friend of the Emperor whom courage is indisputable ",
                    opponentName = "Marshal Lannes",
                    opponentDeckId = "lannes_deck",  // Balanced deck with many tactical cards
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
                    id = "level_7",
                    name = "Emperor Napoleon Bonaparte",
                    description = "Face the Emperor himself in the ultimate battle of wits and strategy!",
                    opponentName = "Napoleon Bonaparte",
                    opponentDeckId = "napoleon_deck",  // A powerful deck with unique cards
                    difficulty = Difficulty.LEGENDARY,
                    specialRules = listOf(
                        SpecialRule.ModifiedMana(2),  // Player gets +2 max mana to have a chance
                        SpecialRule.StartingBoard(
                            listOf(
                                // Napoleon's Imperial Guard
                                BoardSetup(
                                    unitId = 501,
                                    row = 1,
                                    col = 1,
                                    isPlayerUnit = false
                                ),  // Old Guard infantry
                                BoardSetup(
                                    unitId = 502,
                                    row = 1,
                                    col = 3,
                                    isPlayerUnit = false
                                ),  // Imperial Guard cavalry
                                BoardSetup(
                                    unitId = 503,
                                    row = 0,
                                    col = 2,
                                    isPlayerUnit = false
                                )   // Guard artillery
                            )
                        )
                    ),
                    reward = "napoleon_strategy_deck"  // A special deck as final reward
                )
            )
        )
    }

    /**
     * Reset all campaign progress
     */
    fun resetAllProgress() {
        campaigns.keys.forEach { campaignId ->
            prefs.edit().remove("${COMPLETED_LEVELS_KEY}_$campaignId").apply()
        }

        // Reinitialize campaigns with default state
        val napoleonCampaign = createNapoleonCampaign()
        campaigns[napoleonCampaign.id] = napoleonCampaign
    }
}