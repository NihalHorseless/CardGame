package com.example.cardgame.game

import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.model.abilities.TauntManager
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.model.formation.FormationManager

class GameManager {
    val players = listOf(Player(0, "Player 1"), Player(1, "Player 2"))
    val turnManager = TurnManager(this)
    val formationManager = FormationManager()
    val gameBoard =  Board(5, 5) // Create a 5x5 unified board
    val movementManager = MovementManager(gameBoard) // Add movement manager
    val tauntManager = TauntManager(gameBoard)

    // Create player contexts
    val playerContexts = players.associateWith { PlayerContext(it, gameBoard) }

    var gameState: GameState = GameState.NOT_STARTED
    private var winner: Player? = null

    /**
     * Gets the PlayerContext for a specific player.
     */
    fun getPlayerContext(player: Player): PlayerContext {
        return playerContexts[player] ?: throw IllegalArgumentException("Player not found in context map")
    }

    /**
     * Gets the PlayerContext for a player by ID.
     */
    fun getPlayerContextById(playerId: Int): PlayerContext {
        val player = players.find { it.id == playerId }
            ?: throw IllegalArgumentException("Player with ID $playerId not found")
        return getPlayerContext(player)
    }

    fun startGame() {
        // Initialize players
        players.forEach { player ->
            player.health = 30
            player.currentMana = 0
            player.hand.clear()

            // Draw initial hand
            player.drawInitialHand(3)
        }

        // Clear the board
        gameBoard.clearAllUnits()

        gameState = GameState.IN_PROGRESS
        turnManager.startGame()
    }


    fun reset() {
        // Clear player hands
        players.forEach { player ->
            player.hand.clear()
            player.deck.cards.clear()
        }

        // Reset game state
        gameState = GameState.NOT_STARTED

        // Clear the board
        gameBoard.clearAllUnits()
    }

    private fun initializePlayer(player: Player) {
        // Shuffle deck
        player.deck.shuffle()

        // Draw initial hand
        repeat(3) { player.drawCard() }
    }

    fun checkForDestroyedUnits() {
        val destroyedUnits = mutableListOf<UnitCard>()

        // Find dead units
        for (row in 0 until gameBoard.rows) {
            for (col in 0 until gameBoard.columns) {
                val unit = gameBoard.getUnitAt(row, col)
                if (unit != null && unit.isDead()) {
                    destroyedUnits.add(unit)
                }
            }
        }

        // Remove dead units
        destroyedUnits.forEach { unit ->
            val position = gameBoard.getUnitPosition(unit)
            if (position != null) {
                gameBoard.removeUnit(position.first, position.second)
            }
        }

        // Check win condition
        checkWinCondition()
    }

    fun getOpponentOf(player: Player?): Player? {
        if (player == null) return null
        return players.firstOrNull { it != player }
    }

    /**
     * Get the opponent context for a player.
     */
    fun getOpponentContextOf(playerContext: PlayerContext): PlayerContext? {
        val opponent = getOpponentOf(playerContext.player) ?: return null
        return getPlayerContext(opponent)
    }

    fun checkWinCondition() {
        // Game is over if any player's health is 0 or less
        val deadPlayers = players.filter { it.isDead() }
        if (deadPlayers.isNotEmpty()) {
            gameState = GameState.FINISHED

            // Determine winner
            winner = players.firstOrNull { !it.isDead() }
        }
    }

    /**
     * Check if a unit can attack another unit based on game rules.
     * Updated to include adjacent taunt protection logic.
     */
    fun canUnitAttackTarget(attacker: UnitCard, targetRow: Int, targetCol: Int): Boolean {
        // Get the attacker's position
        val attackerPos = gameBoard.getUnitPosition(attacker) ?: return false

        // Get the attacker's owner
        val attackerOwnerId = gameBoard.getUnitOwner(attacker) ?: return false

        // Get the target unit
        val targetUnit = gameBoard.getUnitAt(targetRow, targetCol) ?: return false

        // Get the target's owner
        val targetOwnerId = gameBoard.getUnitOwner(targetUnit) ?: return false

        // Cannot attack own units
        if (attackerOwnerId == targetOwnerId) return false

        // Check if the attacker can attack this turn
        if (!attacker.canAttackThisTurn) return false

        // Check if target is protected by adjacent taunt units
        // This is the main change - we now check if the unit is protected by taunt
        if (tauntManager.isProtectedByTaunt(targetUnit)) {
            return false // Cannot attack units protected by taunt
        }

        // Check attack range (for now, just adjacent cells)
        val (attackerRow, attackerCol) = attackerPos
        val manhattanDistance = Math.abs(attackerRow - targetRow) + Math.abs(attackerCol - targetCol)

        // For basic implementation, only allow attacking adjacent units
        // Could be expanded based on unit type (artillery could attack from distance, etc.)
        return manhattanDistance == 1
    }

    /**
     * Executes an attack between two units.
     */
    /**
     * Executes an attack between two units.
     */
    fun executeAttack(attacker: UnitCard, targetRow: Int, targetCol: Int): Boolean {
        if (!canUnitAttackTarget(attacker, targetRow, targetCol)) return false

        val targetUnit = gameBoard.getUnitAt(targetRow, targetCol) ?: return false

        // Deal damage to target
        targetUnit.takeDamage(attacker.attack)
        // Take damage from target
        attacker.takeDamage(targetUnit.attack)

        // Check for destructions
        checkForDestroyedUnits()

        // Unit has attacked this turn
        attacker.canAttackThisTurn = false

        return true
    }

    /**
     * Executes a direct attack on a player.
     */
    fun executeDirectAttack(attacker: UnitCard, targetPlayerId: Int): Boolean {
        // First, verify that the attacker can perform a direct attack
        val attackerOwnerId = gameBoard.getUnitOwner(attacker) ?: return false

        // Cannot attack self
        if (attackerOwnerId == targetPlayerId) return false

        // Check if the attacker can attack this turn
        if (!attacker.canAttackThisTurn) return false

        // Get the target player
        val targetPlayer = players.find { it.id == targetPlayerId } ?: return false

        // Check if the opponent has any taunt units
        // For direct player attacks, we only need to check if any taunt units exist,
        // not just adjacent ones
        if (tauntManager.hasAnyTauntUnits(targetPlayerId)) {
            return false // Cannot attack player directly if they have any taunt units
        }

        // Get the attacker's position
        val attackerPos = gameBoard.getUnitPosition(attacker) ?: return false
        val (attackerRow, _) = attackerPos

        // Only allow direct player attacks if the unit is at the edge of the board near the opponent
        // For player 0, must be at row 0 to attack player 1
        // For player 1, must be at row 4 to attack player 0
        val canReachPlayer = when (attackerOwnerId) {
            0 -> attackerRow == 0 // Player 0's unit must be at the top row to attack Player 1
            1 -> attackerRow == gameBoard.rows - 1 // Player 1's unit must be at the bottom row to attack Player 0
            else -> false
        }

        if (!canReachPlayer) return false

        // Deal damage to the player
        targetPlayer.takeDamage(attacker.attack)

        // Unit has attacked this turn
        attacker.canAttackThisTurn = false

        // Check win condition
        checkWinCondition()

        return true
    }

    /**
     * Move a unit to a new position
     */
    fun moveUnit(unit: UnitCard, targetRow: Int, targetCol: Int): Boolean {
        return movementManager.moveUnit(unit, targetRow, targetCol)
    }

    /**
     * Get valid movement destinations for a unit
     */
    fun getValidMoveDestinations(unit: UnitCard): List<Pair<Int, Int>> {
        return movementManager.getValidMoveDestinations(unit)
    }
    /**
     * Get all valid attack targets considering taunt protection
     */
    fun getValidAttackTargetsForUnit(unit: UnitCard): List<Pair<Int, Int>> {
        val unitPos = gameBoard.getUnitPosition(unit) ?: return emptyList()
        val (row, col) = unitPos
        val unitOwnerId = gameBoard.getUnitOwner(unit) ?: return emptyList()

        // If unit can't attack, return empty list
        if (!unit.canAttackThisTurn) return emptyList()

        // Get the opponent's player ID
        val opponentId = if (unitOwnerId == 0) 1 else 0

        // Check adjacent cells for potential targets
        val adjacentPositions = listOf(
            Pair(row - 1, col), // above
            Pair(row + 1, col), // below
            Pair(row, col - 1), // left
            Pair(row, col + 1)  // right
        )

        return adjacentPositions.filter { (targetRow, targetCol) ->
            // Check if position is valid and has an enemy unit
            if (targetRow < 0 || targetRow >= gameBoard.rows ||
                targetCol < 0 || targetCol >= gameBoard.columns) {
                return@filter false
            }

            val targetUnit = gameBoard.getUnitAt(targetRow, targetCol)
            if (targetUnit == null || gameBoard.getUnitOwner(targetUnit) != opponentId) {
                return@filter false
            }

            // Important: Taunt units are always valid targets
            // Other units are valid only if not protected by taunt
            targetUnit.hasTaunt || !tauntManager.isProtectedByTaunt(targetUnit)
        }
    }

    /**
     * Check if a unit can move
     */
    fun canUnitMove(unit: UnitCard): Boolean {
        return movementManager.canUnitMove(unit)
    }

    /**
     * Executes an attack using the player contexts.
     */
    fun executeAttackWithContext(
        attackerContext: PlayerContext,
        attackerRow: Int,
        attackerCol: Int,
        targetRow: Int,
        targetCol: Int
    ): Boolean {
        val attacker = gameBoard.getUnitAt(attackerRow, attackerCol) ?: return false
        if (gameBoard.getUnitOwner(attacker) != attackerContext.player.id) return false

        return executeAttack(attacker, targetRow, targetCol)
    }

    /**
     * Executes a direct attack on a player using player contexts.
     */
    fun executeDirectAttackWithContext(
        attackerContext: PlayerContext,
        attackerRow: Int,
        attackerCol: Int
    ): Boolean {
        val attacker = gameBoard.getUnitAt(attackerRow, attackerCol) ?: return false
        if (gameBoard.getUnitOwner(attacker) != attackerContext.player.id) return false

        val opponentId = if (attackerContext.player.id == 0) 1 else 0
        return executeDirectAttack(attacker, opponentId)
    }

    /**
     * Moves a unit using the player contexts.
     */
    fun moveUnitWithContext(
        playerContext: PlayerContext,
        fromRow: Int,
        fromCol: Int,
        toRow: Int,
        toCol: Int
    ): Boolean {
        val unit = gameBoard.getUnitAt(fromRow, fromCol) ?: return false
        if (gameBoard.getUnitOwner(unit) != playerContext.player.id) return false

        return moveUnit(unit, toRow, toCol)
    }
}