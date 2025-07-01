package io.github.nihalhorseless.eternalglory.util

import io.github.nihalhorseless.eternalglory.data.enum.UnitEra
import io.github.nihalhorseless.eternalglory.data.enum.UnitType
import io.github.nihalhorseless.eternalglory.data.model.card.Deck
import io.github.nihalhorseless.eternalglory.data.model.card.UnitCard
import io.github.nihalhorseless.eternalglory.game.Player

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