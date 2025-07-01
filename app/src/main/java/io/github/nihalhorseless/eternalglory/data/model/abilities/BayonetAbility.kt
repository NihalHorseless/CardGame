package io.github.nihalhorseless.eternalglory.data.model.abilities

import io.github.nihalhorseless.eternalglory.data.enum.UnitType
import io.github.nihalhorseless.eternalglory.data.model.card.UnitCard
import io.github.nihalhorseless.eternalglory.game.GameManager


class BayonetAbility : Ability {
    override val name = "Bayonet"
    override val description = "Transform a MUSKET unit into INFANTRY, resetting its movement and attack for this turn"

    override fun apply(unit: UnitCard, gameManager: GameManager) {
        // Only applicable to MUSKET units
        if (unit.unitType != UnitType.MUSKET) return

        // Transform to INFANTRY
        unit.unitType = UnitType.INFANTRY

        // Reset movement and attack capabilities
        unit.canAttackThisTurn = false

        // Mark the unit as moved in the movement manager
        gameManager.movementManager.markUnitAsMoved(unit)

    }
}