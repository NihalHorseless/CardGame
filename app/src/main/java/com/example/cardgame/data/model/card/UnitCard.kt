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
        if (player.currentMana < manaCost) return false
        if (player.board.isFull()) return false

        player.currentMana -= manaCost
        player.hand.remove(this)

        val position = targetPosition ?: player.board.getFirstEmptyPosition()
        player.board.placeUnit(this, position)

        // Initialize attack availability based on charge ability
        canAttackThisTurn = hasCharge

        // Apply formation effects
        gameManager.formationManager.applyFormationEffects(player)

        return true
    }

    /**
     * Attack an enemy unit
     */
    fun attackUnit(target: UnitCard, gameManager: GameManager): Boolean {
        if (!canAttackThisTurn) return false

        // Check for taunt units
        val opponent = gameManager.getOpponentOf(getOwningPlayer(gameManager)) ?: return false
        if (shouldAttackTauntFirst(opponent, target)) {
            return false // Must attack a taunt unit
        }

        // Deal damage to target
        target.takeDamage(attack)
        // Take damage from target
        this.takeDamage(target.attack)

        // Check for destructions
        gameManager.checkForDestroyedUnits()

        // Unit has attacked this turn
        canAttackThisTurn = false

        return true
    }

    /**
     * Attack the enemy player directly
     */
    fun attackOpponent(gameManager: GameManager): Boolean {
        if (!canAttackThisTurn) return false

        // Get the opponent
        val opponent = gameManager.getOpponentOf(getOwningPlayer(gameManager)) ?: return false

        // Check for taunt - cannot attack opponent directly if there are taunt units
        if (opponent.board.hasTauntUnit()) {
            return false
        }

        // Deal damage to opponent
        opponent.takeDamage(attack)

        // Unit has attacked this turn
        canAttackThisTurn = false

        // Check win condition
        gameManager.checkWinCondition()

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

    private fun getOwningPlayer(gameManager: GameManager): Player? {
        return gameManager.players.find { player ->
            player.board.getAllUnits().contains(this)
        }
    }

    private fun shouldAttackTauntFirst(opponent: Player, target: UnitCard): Boolean {
        // Check if there are any taunt units that must be attacked first
        return opponent.board.hasTauntUnit() && !target.hasTaunt
    }
}
