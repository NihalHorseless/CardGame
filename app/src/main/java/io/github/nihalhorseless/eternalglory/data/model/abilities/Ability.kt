package io.github.nihalhorseless.eternalglory.data.model.abilities

import io.github.nihalhorseless.eternalglory.data.model.card.UnitCard
import io.github.nihalhorseless.eternalglory.game.GameManager

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