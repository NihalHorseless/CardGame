package com.example.cardgame.data.model.card

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

abstract class Card(
    val id: Int,
    val name: String,
    val description: String,
    val manaCost: Int,
    val imagePath: String
) {
    abstract fun play(player: Player, gameManager: GameManager, targetPosition: Int? = null): Boolean
}