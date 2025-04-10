package com.example.cardgame.data.model.effect

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

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
