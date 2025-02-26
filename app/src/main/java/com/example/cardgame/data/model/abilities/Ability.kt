package com.example.cardgame.data.model.abilities

import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.game.GameManager

interface Ability {
    val name: String
    val description: String
    fun apply(unit: UnitCard, gameManager: GameManager)
}
class ChargeAbility : Ability {
    override val name = "Charge"
    override val description = "Can attack immediately after being played"

    override fun apply(unit: UnitCard, gameManager: GameManager) {
        unit.hasCharge = true
        unit.canAttackThisTurn = true
    }
}