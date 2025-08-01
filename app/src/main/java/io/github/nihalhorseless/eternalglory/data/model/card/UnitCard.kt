package io.github.nihalhorseless.eternalglory.data.model.card

import io.github.nihalhorseless.eternalglory.data.enum.UnitEra
import io.github.nihalhorseless.eternalglory.data.enum.UnitType
import io.github.nihalhorseless.eternalglory.data.model.abilities.Ability
import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.game.Player

class UnitCard(
    id: Int,
    name: String,
    description: String,
    manaCost: Int,
    imagePath: String,
    var attack: Int,
    var health: Int,
    var maxHealth: Int,
    var unitType: UnitType,           // Combat role of the unit
    val unitEra: UnitEra,
    val abilities: MutableList<Ability> = mutableListOf(),
    var canAttackThisTurn: Boolean = false,
    var hasCharge: Boolean = false,
    var hasTaunt: Boolean = false
) : Card(id, name, description, manaCost, imagePath) {

    override fun play(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        // This method is now handled differently in the Player class
        // We'll use a simplified version for compatibility

        if (player.currentMana < manaCost) return false

        val boardPosition = if (targetPosition != null) {
            // Convert the linear position to a 2D position
            // This is just a simple conversion for backward compatibility
            val row = targetPosition / gameManager.gameBoard.columns
            val col = targetPosition % gameManager.gameBoard.columns
            Pair(row, col)
        } else {
            gameManager.gameBoard.getFirstEmptyPositionInDeploymentZone(player.id) ?: return false
        }

        // Find empty position in player's deployment zone
        val (row, col) = boardPosition

        if (!gameManager.gameBoard.isPositionEmpty(row, col)) return false

        player.currentMana -= manaCost
        player.hand.remove(this)

        // Create a clone of the card to place on the board
        val boardCard = this.clone()

        // Place on the board
        gameManager.gameBoard.placeUnit(boardCard, row, col, player.id)

        // Initialize attack availability based on charge ability
        boardCard.canAttackThisTurn = boardCard.hasCharge

        return true
    }
    fun takeDamage(amount: Int) {
        val actualDamage = maxOf(0, amount)
        health -= actualDamage
    }

    fun heal(amount: Int) {
        health = minOf(maxHealth, health + amount)
    }

    fun isDead(): Boolean = health <= 0

    fun clone(): UnitCard {
        return UnitCard(
            id = id,
            name = name,
            description = description,
            manaCost = manaCost,
            imagePath = imagePath,
            attack = attack,
            health = health,
            maxHealth = maxHealth,
            unitType = unitType,
            unitEra = unitEra,
            abilities = abilities.toMutableList(), // Create a new list to avoid reference issues
            canAttackThisTurn = canAttackThisTurn,
            hasCharge = hasCharge,
            hasTaunt = hasTaunt
        )
    }
}