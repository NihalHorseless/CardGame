package com.example.cardgame.data.model.formation

import com.example.cardgame.game.Player

/**
 * Manages formation detection and effect application.
 */
class FormationManager {
    private val formations = mutableListOf<Formation>()

    fun registerFormation(formation: Formation) {
        formations.add(formation)
    }

    fun getRegisteredFormations(): List<Formation> {
        return formations.toList()
    }

    fun applyFormationEffects(player: Player) {
        getActiveFormations(player).forEach { formation ->
            formation.effects.forEach { effect ->
                effect.apply(player, formation.unitPositions)
            }
        }
    }

    fun getActiveFormations(player: Player): List<Formation> {
        return formations.filter { formation ->
            isFormationActive(player, formation)
        }
    }

    fun isFormationActive(player: Player, formation: Formation): Boolean {
        return formation.unitPositions.all { position ->
            position < player.board.maxSize && player.board.getUnitAt(position) != null
        }
    }

    /**
     * Initializes the formation manager with predefined formations.
     */
    fun initializePredefinedFormations() {
        // Triangle formation
        registerFormation(
            Formation(
                name = "Triangle",
                description = "Units form a triangle for attack bonus",
                unitPositions = listOf(0, 1, 3),
                effects = listOf(AttackBoostEffect(1))
            )
        )


        // V-shape formation
        registerFormation(
            Formation(
                name = "V-Shape",
                description = "Units in V formation get attack bonus",
                unitPositions = listOf(0, 2, 3, 5),
                effects = listOf(AttackBoostEffect(2))
            )
        )

    }
}