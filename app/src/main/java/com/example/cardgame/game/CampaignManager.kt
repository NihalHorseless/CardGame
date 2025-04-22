package com.example.cardgame.game

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.data.model.campaign.SpecialRule
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.CampaignRepository
import com.example.cardgame.data.repository.CardRepository

class CampaignManager(
    private val campaignRepository: CampaignRepository,
    private val gameManager: GameManager,
    private val cardRepository: CardRepository
) {
    private val _currentCampaign = mutableStateOf<Campaign?>(null)
    val currentCampaign: State<Campaign?> = _currentCampaign

    private val _currentLevel = mutableStateOf<CampaignLevel?>(null)
    val currentLevel: State<CampaignLevel?> = _currentLevel

    // Load a campaign
    fun loadCampaign(campaignId: String) {
        _currentCampaign.value = campaignRepository.getCampaign(campaignId)
    }

    // Prepare a level for play
    fun prepareLevel(levelId: String): Boolean {
        val campaign = _currentCampaign.value ?: return false
        val level = campaign.levels.find { it.id == levelId } ?: return false

        // Set the level
        _currentLevel.value = level

        // Configure game with level settings
        configureGameForLevel(level)

        return true
    }

    // Configure the game with level-specific settings
    private fun configureGameForLevel(level: CampaignLevel) {
        // Load the opponent's deck
        val opponentDeck = cardRepository.loadDeck(level.opponentDeckId)
        gameManager.players[1].setDeck(opponentDeck ?: return)

        // Set player and opponent health
        gameManager.players[0].health = level.startingHealth
        gameManager.players[1].health = when(level.difficulty) {
            Difficulty.EASY -> 25
            Difficulty.MEDIUM -> 30
            Difficulty.HARD -> 35
            Difficulty.LEGENDARY -> 40
        }

        // Set starting mana
        gameManager.players[0].currentMana = level.startingMana
        gameManager.players[1].currentMana = level.startingMana

        // Apply special rules
        applySpecialRules(level.specialRules)
    }

    // Apply special rules to the game
    private fun applySpecialRules(rules: List<SpecialRule>) {
        rules.forEach { rule ->
            when(rule) {
                is SpecialRule.StartingBoard -> {
                    rule.unitSetup.forEach { setup ->
                        val card = cardRepository.getCardById(setup.unitId) as? UnitCard ?: return@forEach
                        val playerId = if (setup.isPlayerUnit) 0 else 1
                        gameManager.gameBoard.placeUnit(card.clone(), setup.row, setup.col, playerId)
                    }
                }
                is SpecialRule.AdditionalCards -> {
                    rule.cards.forEach { cardId ->
                        val card = cardRepository.getCardById(cardId) ?: return@forEach
                        gameManager.players[0].hand.add(card)
                    }
                }
                is SpecialRule.ModifiedMana -> {
                    gameManager.players[0].maxMana += rule.amount
                }
                is SpecialRule.CustomObjective -> {
                    // Custom objective will be checked during gameplay
                }
            }
        }
    }

    // Mark a level as completed
    fun completeLevel(levelId: String) {
        val campaign = _currentCampaign.value ?: return
        val updatedLevels = campaign.levels.map {
            if (it.id == levelId) it.copy(isCompleted = true) else it
        }

        _currentCampaign.value = campaign.copy(levels = updatedLevels)
        campaignRepository.updateCampaign(_currentCampaign.value!!)
    }

    // Check if the campaign is completed
    fun isCampaignCompleted(): Boolean {
        val campaign = _currentCampaign.value ?: return false
        return campaign.levels.all { it.isCompleted }
    }
}