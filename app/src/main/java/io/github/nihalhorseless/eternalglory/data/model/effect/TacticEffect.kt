package io.github.nihalhorseless.eternalglory.data.model.effect

import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.game.Player

interface TacticEffect {
    /**
     * Name of the effect for display and identification
     */
    val name: String

    /**
     * Description of what the effect does
     */
    val description: String

    /**
     * Apply the effect to the game
     * @param player The player who used the effect
     * @param gameManager Access to game state and board
     * @param targetPosition Target position (if needed) as a linear index
     * @return true if the effect was successfully applied
     */
    fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean
}
