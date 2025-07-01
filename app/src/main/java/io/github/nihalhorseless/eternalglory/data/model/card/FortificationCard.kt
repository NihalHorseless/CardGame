package io.github.nihalhorseless.eternalglory.data.model.card

import io.github.nihalhorseless.eternalglory.data.enum.FortificationType
import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.game.Player

/**
 * Represents a fortification card that can be deployed on the board.
 * Unlike units, fortifications cannot move but can provide defensive or offensive capabilities.
 */
class FortificationCard(
    id: Int,
    name: String,
    description: String,
    manaCost: Int,
    imagePath: String,
    var attack: Int,
    var health: Int,
    var maxHealth: Int,
    val fortType: FortificationType,
    var canAttackThisTurn: Boolean = false
) : Card(id, name, description, manaCost, imagePath) {

    override fun play(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        // This method is mainly for backward compatibility
        // Actual deployment is handled by the PlayerContext
        return false
    }
    /**
     * Take damage from an attack
     */
    fun takeDamage(amount: Int) {
        val actualDamage = maxOf(0, amount)
        health -= actualDamage
    }

    /**
     * Heal the fortification
     */
    fun heal(amount: Int) {
        health = minOf(maxHealth, health + amount)
    }

    /**
     * Check if the fortification is destroyed
     */
    fun isDestroyed(): Boolean = health <= 0

    /**
     * Create a copy of this fortification
     */
    fun clone(): FortificationCard {
        return FortificationCard(
            id = id,
            name = name,
            description = description,
            manaCost = manaCost,
            imagePath = imagePath,
            attack = attack,
            health = health,
            maxHealth = maxHealth,
            fortType = fortType,
            canAttackThisTurn = canAttackThisTurn
        )
    }
}