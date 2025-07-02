package io.github.nihalhorseless.eternalglory.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import io.github.nihalhorseless.eternalglory.data.model.campaign.BoardSetup
import io.github.nihalhorseless.eternalglory.data.model.campaign.Campaign
import io.github.nihalhorseless.eternalglory.data.model.campaign.CampaignLevel
import io.github.nihalhorseless.eternalglory.data.model.campaign.Difficulty
import io.github.nihalhorseless.eternalglory.data.model.campaign.SpecialRule

/**
 * Repository for accessing and managing campaign data
 */
class CampaignRepository(private val context: Context) {
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
        } catch (_: Exception) {

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
                    name = "The Prince without a Crown",
                    description = "Józef Antoni Poniatowski was born in Vienna 7 May 1763, " +
                            "he was the nephew of the last remaining king of Poland. " +
                            "Despite his Polish heritage, Poniatowski was raised in Vienna where he pursued a military career " +
                            "He had fought for the Austrian army for the war against the Ottomans \n" +
                            "His uncle called him for his services for the war against the Russians which saw the partition of Poland ending the dynasty. " +
                            "Eventually he was exiled which led their paths cross with Napoleon, at last he was promoted to marshal before he died in the battle of Leipzig ",
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
                    name = "The Brutus of Napoleon",
                    description = "Auguste de Marmont was born in Châtillon-sur-Seine 20 July 1774 \n" +
                            "He was the son of a ex-officer in the army who belonged to the petite noblesse. " +
                            "Just like his father he pursued a military career with the supervision of him in Dijon where he met Napoleon. " +
                            "Their paths crossed again in the Siege of Toulon (1793) which led to their mutual friendship as he became Napoleon's aide-de-camp. " +
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
                    name = "Bras de Fer",
                    description = "Jean-de-Dieu Soult was born in Saint-Amans-la-Bastide 29 March 1769 " +
                            "Son of a country notary, Soult wanted to pursue a career in Law but the early passing of his father made him enlist in the Royal Academy when he was 16.  " +
                            "He rose through the ranks quickly during France's revolutionary wars where he worked under future marshals such as Masséna and Jourdan. " +
                            "Eventually his disciplined attitude caught the attention of Napoleon whom promoted him to marshal in 1804",
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
                    name = "The Bravest of the Brave",
                    description = "Michel Ney was born in the small town of Sarrelouis 10 January 1769. " +
                            "Son of a retired veteran, Ney had occupied himself with various civil duties. " +
                            "Eventually he realized that he wanted pursue different things in life and joined Hussar Regiment, where he rose through the ranks rapidly during France's revolutionary wars. " +
                            "His bold and brave actions caught the attention of the Emperor himself which led to his promotion to marshal on 19 May 1804 ",
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
                    name = "The Iron Marshal",
                    description = "Louis-Nicolas d'Avout commonly known as Davout was born in the small village of Annoux  10 May 1770. " +
                            "Son of a cavalry officer, Davout had attended the Military Academy Brienne-le-Chateau along with Napoleon himself. " +
                            "He proved his military brilliance during France's revolutionary wars and the others that followed it. Despite his young age Davout was very quick to rose through the ranks, " +
                            "his disciplined approach to military affairs had earned him the promotion to marshal and the nickname 'Iron Marshal'",
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
                    name = "Achilles of Napoleon",
                    description = "Jean Lannes was born in the small town of Lectoure 10 April 1769 \n" +
                            "Son of a merchant, Lannes enlisted in the army without a proper education but his will strength and sharp character helped him close that gap. " +
                            "He rose through the ranks quickly but had do work for it again due to new army reforms of Thermidorians. " +
                            "Eventually he proved himself again in the campaign of Italy where Napoleon saw how much he inspired his troops leading in the front lines regardless of the danger. This granted him the promotion to Marshal during 1804",
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
                    name = "Vive L'Empereur",
                    description = "Napoleon Bonaparte was born in the island of Corsica on 15 August 1769 as the son of a minor nobility in the Corsican aristocracy.\n" +
                            "He enrolled in the École militaire where he trained to become an artillery officer. " +
                            "His brilliance in organization was immaculate as he showed his brilliance in the siege of Toulon 1793. " +
                            "Climbing rank after rank Napoleon sought every opportunity to prove himself eventually leading campaigns and wars for France. " +
                            "Born from humble backgrounds he found a new Empire, changing the fates of nations along his glorious path",
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