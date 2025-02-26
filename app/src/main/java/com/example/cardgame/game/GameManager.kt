package com.example.cardgame.game

import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.model.formation.FormationManager

class GameManager {
    val players = listOf(Player(0, "Player 1"), Player(1, "Player 2"))
    val turnManager = TurnManager(this)
    val formationManager = FormationManager()
    var gameState: GameState = GameState.NOT_STARTED

    fun startGame() {
        // Initialize players
        players.forEach { player ->
            initializePlayer(player)
        }

        gameState = GameState.IN_PROGRESS
        turnManager.startGame()
    }
    fun reset() {
        // Clear player hands and boards
        players.forEach { player ->
            player.hand.clear()
            player.board.clearAllUnits()
            player.deck.clear()
        }

        // Reset game state
        gameState = GameState.NOT_STARTED
    }


    private fun initializePlayer(player: Player) {
        // Shuffle deck
        player.deck.shuffle()

        // Draw initial hand
        repeat(3) { player.drawCard() }
    }

    fun checkForDestroyedUnits() {
        players.forEach { player ->
            val positions = mutableListOf<Int>()

            // Find dead units
            for (i in 0 until player.board.maxSize) {
                val unit = player.board.getUnitAt(i)
                if (unit != null && unit.isDead()) {
                    positions.add(i)
                }
            }

            // Remove dead units
            positions.forEach { position ->
                player.board.removeUnit(position)
            }

            // Reapply formation effects after units are removed
            formationManager.applyFormationEffects(player)
        }

        // Check win condition
        checkWinCondition()
    }
    fun getOpponentOf(player: Player?): Player? {
        if (player == null) return null
        return players.firstOrNull { it != player }
    }

    fun checkWinCondition() {
        val deadPlayers = players.filter { it.isDead() }
        if (deadPlayers.isNotEmpty()) {
            gameState = GameState.FINISHED
            // Winner is the player who is not dead
           // winner = players.firstOrNull { !it.isDead() }
        }
    }

}