package com.example.cardgame.data.model.campaign

data class Campaign(
    val id: String,
    val name: String,
    val description: String,
    val levels: List<CampaignLevel>,
    val reward: String? = null  // Reward for completing the campaign (e.g., special card)
)
