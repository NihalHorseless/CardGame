package com.example.cardgame.game

import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.UnitCard
import kotlin.math.abs

/**
 * Context object that bundles a player with the game board.
 * For a 5x5 board:
 * - Player 0's deployment zone is rows 3-4
 * - Player 1's deployment zone is rows 0-1
 * - Row 2 is neutral territory
 */
class PlayerContext(val player: Player, val gameBoard: Board) {

    /**
     * Gets all units on the board that belong to this player.
     */
    val units: List<UnitCard>
        get() = gameBoard.getPlayerUnits(player.id)

    /**
     * Gets all fortifications owned by this player.
     */
    val fortifications: List<FortificationCard>
        get() = gameBoard.getPlayerFortifications(player.id)


    /**
     * Gets a unit at a specific board position if it belongs to this player.
     */
    fun getUnitAt(row: Int, col: Int): UnitCard? {
        val unit = gameBoard.getUnitAt(row, col)
        return if (unit != null && gameBoard.getUnitOwner(unit) == player.id) unit else null
    }

    /**
     * Gets the first empty position in the player's deployment zone.
     */
    fun getFirstEmptyPosition(): Pair<Int, Int>? {
        return gameBoard.getFirstEmptyPositionInDeploymentZone(player.id)
    }

    /**
     * Checks if a position is in the player's deployment zone.
     */
    fun isInDeploymentZone(row: Int, col: Int): Boolean {
        return gameBoard.isInDeploymentZone(row, player.id)
    }

    /**
     * Plays a card to the specified position.
     * For unit cards, this places them on the board.
     * For other cards, it delegates to their play method.
     */
    fun playCard(cardIndex: Int, gameManager: GameManager, targetPosition: Pair<Int, Int>? = null): Boolean {
        if (cardIndex < 0 || cardIndex >= player.hand.size) return false

        val card = player.hand[cardIndex]

        // Check if player can afford the card
        if (player.currentMana < card.manaCost) return false

        when (card) {
            is UnitCard -> {
                // Get target position or find an empty one
                val position = targetPosition ?: getFirstEmptyPosition() ?: return false

                // Check if position is in player's deployment zone
                if (!isInDeploymentZone(position.first, position.second)) return false

                // Check if the position is completely empty
                if (!gameBoard.isPositionCompletelyEmpty(position.first, position.second)) return false

                // Pay the mana cost
                player.currentMana -= card.manaCost

                // Remove card from hand
                player.hand.remove(card)

                // Clone the card for the board
                val boardCard = card.clone()

                // Place the cloned card on the unified board
                val (row, col) = position
                val placed = gameBoard.placeUnit(boardCard, row, col, player.id)

                if (placed) {
                    // Initialize attack availability based on charge ability
                    boardCard.canAttackThisTurn = boardCard.hasCharge
                    return true
                }

                // If placement failed, refund the cost and return the card to hand
                player.currentMana += card.manaCost
                player.hand.add(card)
                return false
            }
            is FortificationCard -> {
                // Get target position or find an empty one
                val position = targetPosition ?: getFirstEmptyPosition() ?: return false

                // Check if position is in player's deployment zone
                if (!isInDeploymentZone(position.first, position.second)) return false

                // Check if the position is completely empty
                if (!gameBoard.isPositionCompletelyEmpty(position.first, position.second)) return false

                // Pay the mana cost
                player.currentMana -= card.manaCost

                // Remove card from hand
                player.hand.remove(card)

                // Clone the card for the board
                val boardFortification = card.clone()

                // Place the cloned fortification on the board
                val (row, col) = position
                val placed = gameBoard.placeFortification(boardFortification, row, col, player.id)

                if (placed) {
                    // Initialize attack availability for towers
                    if (boardFortification.fortType == FortificationType.TOWER) {
                        boardFortification.canAttackThisTurn = true
                    }
                    return true
                }

                // If placement failed, refund the cost and return the card to hand
                player.currentMana += card.manaCost
                player.hand.add(card)
                return false
            }
            else -> {
                // For non-unit cards, delegate to the card's play method
                return card.play(player, gameManager, null)
            }
        }
    }

    /**
     * Gets all valid attack targets for a unit at the specified position.
     * Updated to account for adjacent taunt protection.
     */
    fun getValidAttackTargets(row: Int, col: Int, gameManager: GameManager): List<Pair<Int, Int>> {
        val unit = gameBoard.getUnitAt(row, col) ?: return emptyList()
        if (gameBoard.getUnitOwner(unit) != player.id) return emptyList()
        if (!unit.canAttackThisTurn) return emptyList()

        // Get unit targets
        val unitTargets = gameManager.getValidAttackTargetsForUnit(unit)

        // Get fortification targets
        val fortTargets = getValidFortificationTargets(unit, gameManager)

        // Combine both lists
        return unitTargets + fortTargets
    }

    private fun getValidFortificationTargets(unit: UnitCard, gameManager: GameManager): List<Pair<Int, Int>> {
        val unitPos = gameBoard.getUnitPosition(unit) ?: return emptyList()
        val (row, col) = unitPos
        val unitOwnerId = gameBoard.getUnitOwner(unit) ?: return emptyList()

        // If unit can't attack, return empty list
        if (!unit.canAttackThisTurn) return emptyList()

        // Get the opponent's player ID
        val opponentId = if (unitOwnerId == 0) 1 else 0

        // Get the attack range
        val minRange = gameManager.movementManager.getMinAttackRange(unit)
        val maxRange = gameManager.movementManager.getAttackRange(unit)

        val targets = mutableListOf<Pair<Int, Int>>()

        // Check all positions within range for enemy fortifications
        for (targetRow in 0 until gameBoard.rows) {
            for (targetCol in 0 until gameBoard.columns) {
                val distance = abs(row - targetRow) + abs(col - targetCol)
                if (distance in minRange..maxRange) {
                    // Check if position has an enemy fortification
                    val fort = gameBoard.getFortificationAt(targetRow, targetCol)
                    if (fort != null && gameBoard.getFortificationOwner(fort) == opponentId) {
                        targets.add(Pair(targetRow, targetCol))
                    }
                }
            }
        }

        return targets
    }
    /**
     * Checks if a unit can perform a direct attack on the opponent.
     * Updated for new taunt rules - checks if opponent has any taunt units.
     */
    fun canAttackOpponentDirectly(row: Int, col: Int, gameManager: GameManager): Boolean {
        val unit = gameBoard.getUnitAt(row, col) ?: return false
        if (gameBoard.getUnitOwner(unit) != player.id) return false
        if (!unit.canAttackThisTurn) return false

        val opponent = gameManager.getOpponentOf(player) ?: return false

        // Check if opponent has ANY taunt units (not just adjacent ones)
        if (gameManager.tauntManager.hasAnyTauntUnits(opponent.id)) return false

        // Only units at the opponent's edge can attack directly
        return (player.id == 0 && row == 0) || (player.id == 1 && row == gameBoard.rows - 1)
    }

    /**
     * Get valid movement destinations for a unit
     */
    fun getValidMoveDestinations(row: Int, col: Int, gameManager: GameManager): List<Pair<Int, Int>> {
        val unit = gameBoard.getUnitAt(row, col) ?: return emptyList()
        if (gameBoard.getUnitOwner(unit) != player.id) return emptyList()

        return gameManager.getValidMoveDestinations(unit)
    }

    /**
     * Move a unit from one position to another
     */
    fun moveUnit(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, gameManager: GameManager): Boolean {
        return gameManager.moveUnitWithContext(this, fromRow, fromCol, toRow, toCol)
    }

    /**
     * Check if a unit can move
     */
    fun canUnitMove(row: Int, col: Int, gameManager: GameManager): Boolean {
        val unit = gameBoard.getUnitAt(row, col) ?: return false
        if (gameBoard.getUnitOwner(unit) != player.id) return false

        return gameManager.canUnitMove(unit)
    }

    /**
     * Get a list of all units that can move this turn
     */
    fun getMovableUnits(gameManager: GameManager): List<UnitCard> {
        return units.filter { gameManager.canUnitMove(it) }
    }
}