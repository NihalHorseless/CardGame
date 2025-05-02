package com.example.cardgame.game

import android.util.Log
import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.data.model.abilities.BayonetAbility
import com.example.cardgame.data.model.abilities.TauntManager
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.UnitCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class GameManager {
    val players = listOf(Player(0, "Player 1"), Player(1, "Player 2"))
    val turnManager = TurnManager(this)
    val gameBoard = Board(5, 5) // Create a 5x5 unified board
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
        return playerContexts[player]
            ?: throw IllegalArgumentException("Player not found in context map")
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

    fun startCampaignGame(level: CampaignLevel) {
        // Initialize players
        players[0].let {
            it.health = level.startingHealth
            it.currentMana = level.startingMana
            it.hand.clear()
            it.drawInitialHand(3)
        }
        players[1].let {
            it.health = when (level.difficulty) {
                Difficulty.EASY -> 30
                Difficulty.MEDIUM -> 40
                Difficulty.HARD -> 50
                Difficulty.LEGENDARY -> 60
            }
            it.currentMana = level.startingMana
            it.hand.clear()
            it.drawInitialHand(3)
        }
        Log.d("GameManager" ,players[1].health.toString())

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
        CoroutineScope(Dispatchers.Main).launch {
            delay(700)
            // Remove dead units
            destroyedUnits.forEach { unit ->
                val position = gameBoard.getUnitPosition(unit)
                if (position != null) {
                    gameBoard.removeUnit(position.first, position.second)
                }
            }
        }


        // Also check for destroyed fortifications
        checkForDestroyedFortifications()

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
        if (tauntManager.isProtectedByTaunt(targetUnit)) {
            return false // Cannot attack units protected by taunt
        }

        // Calculate Manhattan distance
        val (attackerRow, attackerCol) = attackerPos
        val manhattanDistance = abs(attackerRow - targetRow) + abs(attackerCol - targetCol)

        // Get the attack range limits based on unit type
        val minRange = movementManager.getMinAttackRange(attacker)
        val maxRange = movementManager.getAttackRange(attacker)

        // Check if the target is within attack range
        return manhattanDistance in minRange..maxRange
    }

    fun canUnitAttackFortification(attacker: UnitCard, targetRow: Int, targetCol: Int): Boolean {
        // Get the attacker's position
        val attackerPos = gameBoard.getUnitPosition(attacker) ?: return false

        // Get the attacker's owner
        val attackerOwnerId = gameBoard.getUnitOwner(attacker) ?: return false

        // Get the target fortification
        val targetFort = gameBoard.getFortificationAt(targetRow, targetCol) ?: return false

        // Get the target's owner
        val targetOwnerId = gameBoard.getFortificationOwner(targetFort) ?: return false

        // Cannot attack own fortifications
        if (attackerOwnerId == targetOwnerId) return false

        // Check if the attacker can attack this turn
        if (!attacker.canAttackThisTurn) return false

        // Calculate Manhattan distance
        val (attackerRow, attackerCol) = attackerPos
        val manhattanDistance = abs(attackerRow - targetRow) + abs(attackerCol - targetCol)

        // Get the attack range limits based on unit type
        val minRange = movementManager.getMinAttackRange(attacker)
        val maxRange = movementManager.getAttackRange(attacker)

        // Check if the target is within attack range
        return manhattanDistance in minRange..maxRange
    }

    /**
     * Gets the damage multiplier for an attack based on unit type matchups
     * - Cavalry deals double damage to Missile and Artillery units
     * - Infantry deals double damage to Cavalry
     */
    private fun getDamageMultiplier(attacker: UnitCard, defender: UnitCard): Float {
        return when {
            // Cavalry deals double damage to Missile and Artillery units
            attacker.unitType == UnitType.CAVALRY &&
                    (defender.unitType == UnitType.MISSILE || defender.unitType == UnitType.ARTILLERY || defender.unitType == UnitType.MUSKET) -> 2.0f

            // Infantry deals double damage to Cavalry
            attacker.unitType == UnitType.INFANTRY && defender.unitType == UnitType.CAVALRY -> 2.0f

            // Normal damage for other matchups
            else -> 1.0f
        }
    }

    /**
     * Gets the damage multiplier for an attack against a fortification
     * - Artillery deals double damage to fortifications
     */
    private fun getFortificationDamageMultiplier(attacker: UnitCard): Float {
        return when {
            // Artillery deals double damage to fortifications
            attacker.unitType == UnitType.ARTILLERY -> 2.0f

            // Normal damage for other unit types
            else -> 1.0f
        }
    }

    /**
     * Calculate the actual damage to be dealt based on unit type matchups
     */
    private fun calculateDamage(attacker: UnitCard, defender: UnitCard): Int {
        val baseAttack = attacker.attack
        val multiplier = getDamageMultiplier(attacker, defender)

        // Apply multiplier and round to nearest integer
        return (baseAttack * multiplier).toInt()
    }

    /**
     * Calculate the actual damage to be dealt to a fortification
     */
    private fun calculateFortificationDamage(attacker: UnitCard): Int {
        val baseAttack = attacker.attack
        val multiplier = getFortificationDamageMultiplier(attacker)

        // Apply multiplier and round to nearest integer
        return (baseAttack * multiplier).toInt()
    }

    /**
     * Check if the attack will have a counter bonus (dealing extra damage)
     */
    fun hasCounterBonus(attacker: UnitCard, defender: UnitCard): Boolean {
        return getDamageMultiplier(attacker, defender) > 1.0f
    }

    /**
     * Check if the attack on a fortification will have a counter bonus (dealing extra damage)
     */
    fun hasFortificationCounterBonus(attacker: UnitCard): Boolean {
        return getFortificationDamageMultiplier(attacker) > 1.0f
    }

    /**
     * Get a description of why the counter bonus applies
     */
    fun getCounterDescription(attacker: UnitCard, defender: UnitCard): String? {
        return when {
            attacker.unitType == UnitType.CAVALRY &&
                    (defender.unitType == UnitType.MISSILE || defender.unitType == UnitType.ARTILLERY) ->
                "Cavalry is effective against ${defender.unitType.name.lowercase()} units!"

            attacker.unitType == UnitType.INFANTRY && defender.unitType == UnitType.CAVALRY ->
                "Infantry is effective against cavalry units!"

            else -> null
        }
    }


    /**
     * Executes an attack between units and/or fortifications.
     * This method handles all attack scenarios:
     * - Unit attacking a unit
     * - Unit attacking a fortification
     */
     fun executeAttack(attacker: UnitCard, targetRow: Int, targetCol: Int): Boolean {
        // Get the target unit (if any)
        val targetUnit = gameBoard.getUnitAt(targetRow, targetCol)

        // Get the target fortification (if any)
        val targetFort = gameBoard.getFortificationAt(targetRow, targetCol)

        // If neither a unit nor a fortification is at the target position, the attack fails
        if (targetUnit == null && targetFort == null) return false

        // If there's a unit at the target position, execute a unit attack
        if (targetUnit != null) {
            // Check if the attack is valid
            if (!canUnitAttackTarget(attacker, targetRow, targetCol)) return false

            // Calculate damage with counter system
            val damage = calculateDamage(attacker, targetUnit)

            // Deal damage to target
            targetUnit.takeDamage(damage)

            // Check if this is a ranged attack (based on Manhattan distance)
            val attackerPos = gameBoard.getUnitPosition(attacker) ?: return false
            val (attackerRow, attackerCol) = attackerPos
            val manhattanDistance = abs(attackerRow - targetRow) + abs(attackerCol - targetCol)

            // Only take counterattack damage if the attack is melee range (distance = 1) and not artillery
            if (manhattanDistance == 1 && targetUnit.unitType != UnitType.ARTILLERY) {
                // Take damage from target's counterattack - also apply counter system
                val counterAttackDamage = calculateDamage(targetUnit, attacker)
                attacker.takeDamage(counterAttackDamage)
            }
        }
        // Otherwise, if there's a fortification at the target position, execute a fortification attack
        else if (targetFort != null) {
            // Check if the attack is valid
            if (!canUnitAttackFortification(attacker, targetRow, targetCol)) return false

            // Calculate damage with fortification counter system
            val damage = calculateFortificationDamage(attacker)

            // Deal damage to fortification
            targetFort.takeDamage(damage)
        }

        // Unit has attacked this turn (in either case)
        attacker.canAttackThisTurn = false

        // Check for destructions
        checkForDestroyedUnits()
        checkForDestroyedFortifications()

        return true
    }

     fun executeFortificationAttack(
        fortification: FortificationCard,
        targetRow: Int,
        targetCol: Int
    ): Boolean {
        // Only towers can attack
        if (fortification.fortType != FortificationType.TOWER) return false

        // Check if fortification can attack this turn
        if (!fortification.canAttackThisTurn) return false

        // Get the fortification's position
        val fortPos = gameBoard.getFortificationPosition(fortification) ?: return false
        val (fortRow, fortCol) = fortPos

        // Get fortification's owner
        val fortOwner = gameBoard.getFortificationOwner(fortification) ?: return false

        // Get the target unit
        val targetUnit = gameBoard.getUnitAt(targetRow, targetCol) ?: return false

        // Can only attack enemy units
        val targetUnitOwner = gameBoard.getUnitOwner(targetUnit) ?: return false
        if (targetUnitOwner == fortOwner) return false

        // Check if target is in range (towers have range 2)
        val distance = abs(fortRow - targetRow) + abs(fortCol - targetCol)
        if (distance > 2) return false // Tower range is 2

        // Deal damage to target (no counter-attack since fortifications are static)
        targetUnit.takeDamage(fortification.attack)

        // Fortification has attacked this turn
        fortification.canAttackThisTurn = false

        // Check for destroyed units
        checkForDestroyedUnits()

        return true
    }

    /**
     * Executes an attack by a unit against a fortification.
     */
    fun executeUnitAttackFortification(
        attacker: UnitCard,
        targetRow: Int,
        targetCol: Int
    ): Boolean {
        if (!canUnitAttackFortification(attacker, targetRow, targetCol)) return false

        val targetFort = gameBoard.getFortificationAt(targetRow, targetCol) ?: return false

        // Calculate damage with fortification counter system
        val damage = calculateFortificationDamage(attacker)

        // Deal damage to fortification
        targetFort.takeDamage(damage)

        // Unit has attacked this turn
        attacker.canAttackThisTurn = false

        // Check for destroyed fortifications
        checkForDestroyedFortifications()

        return true
    }

    fun checkForDestroyedFortifications() {
        // Find destroyed fortifications
        val destroyedFortifications = mutableListOf<FortificationCard>()

        for (row in 0 until gameBoard.rows) {
            for (col in 0 until gameBoard.columns) {
                val fortification = gameBoard.getFortificationAt(row, col)
                if (fortification != null && fortification.isDestroyed()) {
                    destroyedFortifications.add(fortification)
                }
            }
        }

        // Remove destroyed fortifications
        destroyedFortifications.forEach { fortification ->
            val position = gameBoard.getFortificationPosition(fortification)
            if (position != null) {
                gameBoard.removeFortification(position.first, position.second)
            }
        }
    }

    /**
     * Gets valid deployment positions for a player based on their ID.
     * Player 0 can deploy in the first two rows (0-1)
     * Player 1 can deploy in the last two rows (3-4 in a 5x5 board)
     */
    fun getValidDeploymentPositions(playerId: Int): List<Pair<Int, Int>> {
        val validPositions = mutableListOf<Pair<Int, Int>>()

        // Define row ranges based on player ID
        val rowRange = if (playerId == 0) {
            (gameBoard.rows - 2) until gameBoard.rows // Bottom two rows for player 0
        } else {
            0 until 2 // Top two rows for player 1
        }

        // Add all COMPLETELY empty cells in the valid rows
        for (row in rowRange) {
            for (col in 0 until gameBoard.columns) {
                if (gameBoard.isPositionCompletelyEmpty(row, col)) {
                    validPositions.add(Pair(row, col))
                }
            }
        }

        return validPositions
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
     * Get all valid attack targets considering taunt protection and range
     */
    fun getValidAttackTargetsForUnit(unit: UnitCard): List<Pair<Int, Int>> {
        val unitPos = gameBoard.getUnitPosition(unit) ?: return emptyList()
        val (row, col) = unitPos
        val unitOwnerId = gameBoard.getUnitOwner(unit) ?: return emptyList()

        // If unit can't attack, return empty list
        if (!unit.canAttackThisTurn) return emptyList()

        // Get the opponent's player ID
        val opponentId = if (unitOwnerId == 0) 1 else 0

        // Get the attack range based on unit type
        val minRange = movementManager.getMinAttackRange(unit)
        val maxRange = movementManager.getAttackRange(unit)

        // Generate all possible positions within attack range
        val potentialTargets = mutableListOf<Pair<Int, Int>>()

        // Check all positions within Manhattan distance between min and max range
        for (targetRow in 0 until gameBoard.rows) {
            for (targetCol in 0 until gameBoard.columns) {
                val distance = abs(row - targetRow) + abs(col - targetCol)
                if (distance in minRange..maxRange) {
                    potentialTargets.add(Pair(targetRow, targetCol))
                }
            }
        }

        return potentialTargets.filter { (targetRow, targetCol) ->
            // Check if position has an enemy unit
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

    fun registerTemporaryEffect(target: Any, duration: Int, removalAction: () -> Unit) {
        // In a real implementation, this would store the effect in a data structure
        // and remove it after the specified number of turns
        // For this example, we'll just print a message
        println("Registered temporary effect on $target for $duration turns")
    }

    fun registerMovementBuff(unit: UnitCard, moveBoost: Int, duration: Int) {
        // In a real implementation, this would modify the unit's movement range
        // and reset it after the specified number of turns
        // For this example, we'll just print a message
        println("Boosted movement range of ${unit.name} by $moveBoost for $duration turns")
    }
    fun attachBayonetToUnit(unit: UnitCard) {
        // Only allow attaching to MUSKET units
        if (unit.unitType != UnitType.MUSKET) {
            return
        }

        // Create a new BayonetAbility
        val bayonetAbility = BayonetAbility()

        // Apply the ability
        bayonetAbility.apply(unit, this)

        // Add the ability to the unit's list of abilities if you want to track it
        // Note: This requires making the abilities list mutable
        if (unit.abilities is MutableList) {
            (unit.abilities as MutableList<Ability>).add(bayonetAbility)
        }

        // Notify any listeners that the unit has changed
        // This could trigger UI updates
        // For example: notifyUnitChanged(unit)
    }
}