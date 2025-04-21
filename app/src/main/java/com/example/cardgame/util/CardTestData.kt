package com.example.cardgame.util

import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.abilities.Ability
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
        abilities = emptyList<Ability>(),
        maxHealth = 1,
        hasCharge = false,
        imagePath = ""
    )
    val samplePlayer = Player(id = 999, name = "SAMPLE PLAYER")
}