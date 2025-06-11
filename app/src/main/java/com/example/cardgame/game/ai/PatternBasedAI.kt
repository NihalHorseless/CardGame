package com.example.cardgame.game.ai

import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.enum.TargetType
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.game.Board
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.PlayerContext
import kotlin.math.abs

class PatternBasedAI(
    private val difficulty: Difficulty,
    private val gameManager: GameManager,
    private val cardRepository: CardRepository
) {
    private val playerId = 1 // AI is always player 1
    private val opponentId = 0

    fun executeTurn() {
        val context = gameManager.getPlayerContextById(playerId)
        val board = gameManager.gameBoard

        when (difficulty) {
            Difficulty.EASY -> executeEasyAITurn(context, board)
            Difficulty.MEDIUM -> TODO("Implement later")
            Difficulty.HARD -> TODO("Implement later")
            else -> executeEasyAITurn(context, board) // Fallback
        }
    }

    private fun executeEasyAITurn(context: PlayerContext, board: Board) {
        // Phase 1: Play cards randomly
        playCardsRandomly(context)

        // Phase 2: Move units aggressively
        moveUnitsAggressively(context, board)

        // Phase 3: Attack with everything
        attackWithAllUnits(context, board)

        // Phase 4: Attack with fortifications
        attackWithFortifications(context, board)
    }

    /**
     * EASY AI: Play cards whenever possible, randomly
     */
    private fun playCardsRandomly(context: PlayerContext) {
        val player = context.player

        // Keep trying to play cards until we can't
        var cardPlayed = true
        while (cardPlayed && player.hand.isNotEmpty()) {
            cardPlayed = false

            // Shuffle hand to play randomly
            val shuffledIndices = player.hand.indices.shuffled()

            for (cardIndex in shuffledIndices) {
                if (cardIndex >= player.hand.size) continue

                val card = player.hand[cardIndex]
                if (card.manaCost > player.currentMana) continue

                when (card) {
                    is UnitCard -> {
                        // Deploy units on second row (row 1 for AI)
                        val deployed = deployUnitEasy(context, cardIndex, targetRow = 1)
                        if (deployed) {
                            cardPlayed = true
                            break
                        }
                    }
                    is FortificationCard -> {
                        // Deploy fortifications on first row (row 0 for AI)
                        val deployed = deployFortificationEasy(context, cardIndex, targetRow = 0)
                        if (deployed) {
                            cardPlayed = true
                            break
                        }
                    }
                    is TacticCard -> {
                        // Play tactic cards with random valid targets
                        val played = playTacticCardEasy(context, cardIndex, card)
                        if (played) {
                            cardPlayed = true
                            break
                        }
                    }
                }
            }
        }
    }

    /**
     * Deploy unit randomly on specified row
     */
    private fun deployUnitEasy(context: PlayerContext, cardIndex: Int, targetRow: Int): Boolean {
        val availableColumns = (0 until gameManager.gameBoard.columns)
            .filter { col ->
                gameManager.gameBoard.isPositionCompletelyEmpty(targetRow, col)
            }
            .shuffled() // Randomize deployment

        for (col in availableColumns) {
            val deployed = context.playCard(cardIndex, gameManager, Pair(targetRow, col))
            if (deployed) return true
        }

        // If target row is full, try any valid position
        val allValidPositions = gameManager.getValidDeploymentPositions(playerId).shuffled()
        for (pos in allValidPositions) {
            val deployed = context.playCard(cardIndex, gameManager, pos)
            if (deployed) return true
        }

        return false
    }

    /**
     * Deploy fortification randomly on first row
     */
    private fun deployFortificationEasy(context: PlayerContext, cardIndex: Int, targetRow: Int): Boolean {
        // Same logic as units but for fortifications
        return deployUnitEasy(context, cardIndex, targetRow)
    }

    /**
     * Play tactic cards with random targets
     */
    private fun playTacticCardEasy(context: PlayerContext, cardIndex: Int, card: TacticCard): Boolean {
        when (card.targetType) {
            TargetType.NONE -> {
                // Cards like "Draw 2" that don't need targets
                return card.play(context.player, gameManager, null)
            }
            TargetType.ENEMY -> {
                // Find random enemy unit
                val enemyTargets = getAllEnemyTargets().shuffled()
                for (target in enemyTargets) {
                    val linearPos = target.first * gameManager.gameBoard.columns + target.second
                    if (card.play(context.player, gameManager, linearPos)) {
                        return true
                    }
                }
            }
            TargetType.FRIENDLY -> {
                // Find random friendly unit
                val friendlyTargets = getAllFriendlyTargets().shuffled()
                for (target in friendlyTargets) {
                    val linearPos = target.first * gameManager.gameBoard.columns + target.second
                    if (card.play(context.player, gameManager, linearPos)) {
                        return true
                    }
                }
            }
            TargetType.BOARD, TargetType.ANY -> {
                // Random board position
                val allPositions = mutableListOf<Pair<Int, Int>>()
                for (row in 0 until gameManager.gameBoard.rows) {
                    for (col in 0 until gameManager.gameBoard.columns) {
                        allPositions.add(Pair(row, col))
                    }
                }

                for (pos in allPositions.shuffled()) {
                    val linearPos = pos.first * gameManager.gameBoard.columns + pos.second
                    if (card.play(context.player, gameManager, linearPos)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * EASY AI: Move units aggressively toward player, spread wide
     */
    private fun moveUnitsAggressively(context: PlayerContext, board: Board) {
        val movableUnits = context.getMovableUnits(gameManager)

        // Sort units by column to ensure spreading
        val sortedUnits = movableUnits.sortedBy { unit ->
            board.getUnitPosition(unit)?.second ?: 0
        }

        // Track occupied columns to maintain spread
        val occupiedColumns = mutableSetOf<Int>()

        for (unit in sortedUnits) {
            val currentPos = board.getUnitPosition(unit) ?: continue
            val validMoves = gameManager.getValidMoveDestinations(unit)

            if (validMoves.isEmpty()) continue

            // Find the most aggressive move (lowest row for AI) that maintains spread
            val bestMove = validMoves
                .filter { move ->
                    // Prefer moves to unoccupied columns for spreading
                    !occupiedColumns.contains(move.second) ||
                            occupiedColumns.size >= board.columns - 1
                }
                .minByOrNull { move ->
                    // Priority: 1) Move down (toward player), 2) Maintain spread
                    move.first * 10 + if (occupiedColumns.contains(move.second)) 5 else 0
                }
                ?: validMoves.minByOrNull { it.first } // Fallback: just move down

            bestMove?.let { targetPos ->
                // Execute move
                val moved = context.moveUnit(
                    currentPos.first,
                    currentPos.second,
                    targetPos.first,
                    targetPos.second,
                    gameManager
                )

                if (moved) {
                    occupiedColumns.add(targetPos.second)
                }
            }
        }
    }

    /**
     * EASY AI: Attack any valid target without considering counters
     */
    private fun attackWithAllUnits(context: PlayerContext, board: Board) {
        val units = context.units.filter { it.canAttackThisTurn }

        for (unit in units) {
            val unitPos = board.getUnitPosition(unit) ?: continue
            val validTargets = context.getValidAttackTargets(unitPos.first, unitPos.second, gameManager)

            if (validTargets.isEmpty()) {
                // Check if can attack player directly
                if (context.canAttackOpponentDirectly(unitPos.first, unitPos.second, gameManager)) {
                    gameManager.executeDirectAttackWithContext(context, unitPos.first, unitPos.second)
                    continue
                }
            } else {
                // Attack random valid target (no counter consideration)
                val randomTarget = validTargets.random()
                gameManager.executeAttackWithContext(
                    context,
                    unitPos.first,
                    unitPos.second,
                    randomTarget.first,
                    randomTarget.second
                )
            }
        }
    }

    /**
     * EASY AI: Attack with fortifications
     */
    private fun attackWithFortifications(context: PlayerContext, board: Board) {
        val towers = context.fortifications
            .filter { it.fortType == FortificationType.TOWER && it.canAttackThisTurn }

        for (tower in towers) {
            val towerPos = board.getFortificationPosition(tower) ?: continue

            // Find any enemy unit in range (2 for towers)
            val targetsInRange = mutableListOf<Pair<Int, Int>>()

            for (row in 0 until board.rows) {
                for (col in 0 until board.columns) {
                    val distance = abs(towerPos.first - row) + abs(towerPos.second - col)

                    if (distance in 1..2) {
                        val unit = board.getUnitAt(row, col)
                        if (unit != null && board.getUnitOwner(unit) == opponentId) {
                            targetsInRange.add(Pair(row, col))
                        }
                    }
                }
            }

            // Attack random target
            if (targetsInRange.isNotEmpty()) {
                val randomTarget = targetsInRange.random()
                gameManager.executeFortificationAttack(
                    tower,
                    randomTarget.first,
                    randomTarget.second
                )
            }
        }
    }

    // Helper methods
    private fun getAllEnemyTargets(): List<Pair<Int, Int>> {
        val targets = mutableListOf<Pair<Int, Int>>()
        val board = gameManager.gameBoard

        for (row in 0 until board.rows) {
            for (col in 0 until board.columns) {
                val unit = board.getUnitAt(row, col)
                if (unit != null && board.getUnitOwner(unit) == opponentId) {
                    targets.add(Pair(row, col))
                }

                val fort = board.getFortificationAt(row, col)
                if (fort != null && board.getFortificationOwner(fort) == opponentId) {
                    targets.add(Pair(row, col))
                }
            }
        }

        return targets
    }

    private fun getAllFriendlyTargets(): List<Pair<Int, Int>> {
        val targets = mutableListOf<Pair<Int, Int>>()
        val board = gameManager.gameBoard

        for (row in 0 until board.rows) {
            for (col in 0 until board.columns) {
                val unit = board.getUnitAt(row, col)
                if (unit != null && board.getUnitOwner(unit) == playerId) {
                    targets.add(Pair(row, col))
                }

                val fort = board.getFortificationAt(row, col)
                if (fort != null && board.getFortificationOwner(fort) == playerId) {
                    targets.add(Pair(row, col))
                }
            }
        }

        return targets
    }
}