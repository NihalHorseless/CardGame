package com.example.cardgame.game

import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.model.formation.FormationManager

class GameManager {
    val players = listOf(Player(0, "Player 1"), Player(1, "Player 2"))
    val turnManager = TurnManager(this)
    val formationManager = FormationManager()
    var gameState: GameState = GameState.NOT_STARTED
    private var winner: Player? = null

    fun startGame() {
        // Initialize players
        players.forEach { player ->
            player.health = 30
            player.currentMana = 0
            player.hand.clear()
            player.board.clearAllUnits()

            // Draw initial hand
            player.drawInitialHand(3)
        }

        gameState = GameState.IN_PROGRESS
        turnManager.startGame()
    }
    fun reset() {
        // Clear player hands and boards
        players.forEach { player ->
            player.hand.clear()
            player.board.clearAllUnits()
            player.deck.cards.clear()
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

            // Reapply formation effects
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
        // Game is over if any player's health is 0 or less
        val deadPlayers = players.filter { it.isDead() }
        if (deadPlayers.isNotEmpty()) {
            gameState = GameState.FINISHED

            // Determine winner
            winner = players.firstOrNull { !it.isDead() }
        }
    }

}