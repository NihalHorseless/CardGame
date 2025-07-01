package io.github.nihalhorseless.eternalglory.data.model.card

import io.github.nihalhorseless.eternalglory.data.enum.TacticCardType
import io.github.nihalhorseless.eternalglory.data.enum.TargetType
import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.game.Player

class TacticCard(
    id: Int,
    name: String,
    description: String,
    manaCost: Int,
    imagePath: String,
    val cardType: TacticCardType,
    val targetType: TargetType,
    val effect: (Player, GameManager, Int?) -> Boolean
) : Card(id, name, description, manaCost, imagePath) {

    override fun play(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        // Check if player has enough mana
        if (player.currentMana < manaCost) return false

        // Check if a target is required and provided
        if (targetType != TargetType.NONE && targetPosition == null) {
            return false
        }

        // Apply the effect
        val effectApplied = effect(player, gameManager, targetPosition)

        // If effect was successfully applied, consume mana and remove card from hand
        if (effectApplied) {
            player.currentMana -= manaCost
            player.hand.remove(this)
        }

        return effectApplied
    }

    /**
     * Creates a copy of this card
     */
    fun copy(): TacticCard {
        return TacticCard(
            id = id,
            name = name,
            description = description,
            manaCost = manaCost,
            imagePath = imagePath,
            cardType = cardType,
            targetType = targetType,
            effect = effect
        )
    }
}