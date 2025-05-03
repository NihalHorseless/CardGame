package com.example.cardgame.data.model.effect

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

object TacticEffectFactory {

    /**
     * Create a TacticEffect from a type string and value parameter
     *
     * @param effectType The type of effect to create
     * @param effectValue The primary numeric value for the effect (damage, healing, etc.)
     * @param duration Optional duration for effects that last multiple turns
     * @param radius Optional radius for area effects
     * @return A TacticEffect implementation based on the provided parameters
     */
    fun createEffect(
        effectType: String,
        effectValue: Int,
        duration: Int = 1,
        radius: Int = 1
    ): TacticEffect {
        return when (effectType.lowercase()) {
            "direct_damage" -> DirectDamageEffect(effectValue)
            "area_damage" -> AreaDamageEffect(effectValue, radius)
            "healing" -> SingleTargetHealingEffect(effectValue)
            "area_healing" -> AreaHealingEffect(effectValue, radius)
            "buff_attack" -> AttackBuffEffect(effectValue, duration)
            "buff_health" -> HealthBuffEffect(effectValue,duration)
            "draw_cards" -> DrawCardsEffect(effectValue)
            "charge" -> GrantChargeEffect()
            "taunt" -> GrantTauntEffect()
            "refresh_movement" -> RefreshMovementEffect()
            "weaken_unit" -> WeakenUnitEffect()
            "petrify" -> PetrifyUnitEffect()
            "bribery" -> BribeUnitEffect() // New effect type
            else -> createNoOpEffect()
        }
    }

    // Other methods remain the same...

    /**
     * Create a compound effect composed of multiple effects
     *
     * @param effects The list of effects to combine
     * @return A TacticEffect that applies all the given effects
     */
    fun createCompoundEffect(effects: List<TacticEffect>): TacticEffect {
        return object : TacticEffect {
            override val name = "Compound Effect"
            override val description = effects.joinToString(" and ") { it.description }

            override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
                // Apply all effects and return true if any effect was applied
                return effects.map { it.apply(player, gameManager, targetPosition) }
                    .any { it }
            }
        }
    }

    /**
     * Create a function that can be used with TacticCard's effect parameter
     *
     * @param effect The TacticEffect to convert
     * @return A function that applies the effect
     */
    fun createEffectFunction(effect: TacticEffect): (Player, GameManager, Int?) -> Boolean {
        return { player, gameManager, targetPosition ->
            effect.apply(player, gameManager, targetPosition)
        }
    }

    /**
     * Create a no-op effect that does nothing
     */
    private fun createNoOpEffect(): TacticEffect {
        return object : TacticEffect {
            override val name = "No Effect"
            override val description = "This card has no effect"

            override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
                return true // Always succeeds but does nothing
            }
        }
    }
}