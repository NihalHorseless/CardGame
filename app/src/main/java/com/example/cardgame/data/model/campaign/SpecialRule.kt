package com.example.cardgame.data.model.campaign

import com.example.cardgame.game.GameManager

sealed class SpecialRule {
    data class StartingBoard(val unitSetup: List<BoardSetup>) : SpecialRule()
    data class AdditionalCards(val cards: List<Int>) : SpecialRule()
    data class ModifiedMana(val amount: Int) : SpecialRule()
    data class CustomObjective(val description: String, val checkCompletion: (GameManager) -> Boolean) : SpecialRule()
    // Add more rule types as needed
}