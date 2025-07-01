package io.github.nihalhorseless.eternalglory.data.model.card

import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.game.Player

abstract class Card(
    val id: Int,
    val name: String,
    val description: String,
    val manaCost: Int,
    val imagePath: String
) {
    abstract fun play(player: Player, gameManager: GameManager, targetPosition: Int? = null): Boolean
}