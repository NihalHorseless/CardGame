package com.example.cardgame.data.model.card

import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

class UnitCard(
    id: Int,
    name: String,
    description: String,
    manaCost: Int,
    imagePath: String,
    var attack: Int,
    var health: Int,
    var maxHealth: Int,
    val unitType: UnitType,           // Combat role of the unit
    val unitEra: UnitEra,
    val abilities: List<Ability> = emptyList(),
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

    /**
     * Attack an enemy unit at the specified position
     */
    fun attackUnit(targetRow: Int, targetCol: Int, gameManager: GameManager): Boolean {
        return gameManager.executeAttack(this, targetRow, targetCol)
    }

    /**
     * Attack the enemy player directly
     */
    fun attackOpponent(gameManager: GameManager): Boolean {
        // Determine the owner of this unit
        val ownerId = gameManager.gameBoard.getUnitOwner(this) ?: return false

        // Get the opponent's player ID
        val opponentId = if (ownerId == 0) 1 else 0

        return gameManager.executeDirectAttack(this, opponentId)
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
            abilities = abilities.toList(), // Create a new list to avoid reference issues
            canAttackThisTurn = canAttackThisTurn,
            hasCharge = hasCharge,
            hasTaunt = hasTaunt
        )
    }
}