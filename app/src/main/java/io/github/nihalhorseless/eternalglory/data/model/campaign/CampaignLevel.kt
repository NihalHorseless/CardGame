package io.github.nihalhorseless.eternalglory.data.model.campaign

data class CampaignLevel(
    val id: String,
    val name: String,
    val description: String,
    val opponentName: String,
    val opponentDeckId: String,
    val difficulty: Difficulty,
    val specialRules: List<SpecialRule> = emptyList(),
    val isCompleted: Boolean = false,
    val reward: String? = null,  // Reward for completing this level (e.g., card, deck)
    val startingMana: Int = 1,  // Allow for customizing player's starting mana
    val startingHealth: Int = 30  // Allow for customizing player's starting health
)
