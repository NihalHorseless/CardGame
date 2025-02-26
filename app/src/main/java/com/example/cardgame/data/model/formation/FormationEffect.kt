package com.example.cardgame.data.model.formation

import com.example.cardgame.game.Player

interface FormationEffect {
    fun apply(player: Player, unitPositions: List<Int>)
}
class AttackBoostEffect(private val amount: Int) : FormationEffect {
    override fun apply(player: Player, unitPositions: List<Int>) {
        unitPositions.forEach { position ->
            player.board.getUnitAt(position)?.let { unit ->
                // Apply temporary attack boost
                // This would need additional logic to track boosts
            }
        }
    }
}