package com.example.cardgame.util

import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.game.Player

object CardTestData {
    val sampleUnitCard = UnitCard(
        id = 0,
        name = "Sample",
        unitType = UnitType.INFANTRY,
        unitEra = UnitEra.MEDIEVAL,
        description = "",
        attack = 0,
        health = 0,
        hasTaunt = false,
        manaCost = 1,
        canAttackThisTurn = false,
        abilities = mutableListOf(),
        maxHealth = 1,
        hasCharge = false,
        imagePath = ""
    )
    val samplePlayer = Player(id = 999, name = "SAMPLE PLAYER")
    val sampleDeck = Deck(id = "test", description = "", name = "Test" ,cards = mutableListOf())

}