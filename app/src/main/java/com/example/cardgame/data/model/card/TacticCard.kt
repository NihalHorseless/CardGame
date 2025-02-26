package com.example.cardgame.data.model.card

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

class TacticCard(
    id: Int,
    name: String,
    description: String,
    manaCost: Int,
    imagePath: String,
    val effect: (Player, GameManager, Int?) -> Boolean
) : Card(id, name, description, manaCost, imagePath) {

    override fun play(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (player.currentMana < manaCost) return false

        val effectApplied = effect(player, gameManager, targetPosition)

        if (effectApplied) {
            player.currentMana -= manaCost
            player.hand.remove(this)
        }

        return effectApplied
    }
}