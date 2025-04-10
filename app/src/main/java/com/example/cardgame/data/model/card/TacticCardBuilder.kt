package com.example.cardgame.data.model.card

import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.enum.TargetType
import com.example.cardgame.data.model.effect.AreaDamageEffect
import com.example.cardgame.data.model.effect.AreaHealingEffect
import com.example.cardgame.data.model.effect.AttackBuffEffect
import com.example.cardgame.data.model.effect.DirectDamageEffect
import com.example.cardgame.data.model.effect.DrawCardsEffect
import com.example.cardgame.data.model.effect.GrantChargeEffect
import com.example.cardgame.data.model.effect.GrantTauntEffect
import com.example.cardgame.data.model.effect.RefreshMovementEffect
import com.example.cardgame.data.model.effect.SingleTargetHealingEffect
import com.example.cardgame.data.model.effect.TacticEffect
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

/**
 * Builder class for creating TacticCard instances.
 * Makes it easy to create cards with a fluent interface.
 */
class TacticCardBuilder {
    private var id: Int = 0
    private var name: String = ""
    private var description: String = ""
    private var manaCost: Int = 0
    private var imagePath: String = ""
    private var cardType: TacticCardType = TacticCardType.SPECIAL
    private var targetType: TargetType = TargetType.NONE
    private var effect: TacticEffect? = null

    /**
     * Set the card ID
     */
    fun withId(id: Int): TacticCardBuilder {
        this.id = id
        return this
    }

    /**
     * Set the card name
     */
    fun withName(name: String): TacticCardBuilder {
        this.name = name
        return this
    }

    /**
     * Set the card description
     */
    fun withDescription(description: String): TacticCardBuilder {
        this.description = description
        return this
    }

    /**
     * Set the card mana cost
     */
    fun withManaCost(cost: Int): TacticCardBuilder {
        this.manaCost = cost
        return this
    }

    /**
     * Set the card image path
     */
    fun withImagePath(path: String): TacticCardBuilder {
        this.imagePath = path
        return this
    }

    /**
     * Set the card type
     */
    fun withCardType(type: TacticCardType): TacticCardBuilder {
        this.cardType = type
        return this
    }

    /**
     * Set the card target type
     */
    fun withTargetType(type: TargetType): TacticCardBuilder {
        this.targetType = type
        return this
    }

    /**
     * Set the card effect using a TacticEffect instance
     */
    fun withEffect(effect: TacticEffect): TacticCardBuilder {
        this.effect = effect
        return this
    }

    /**
     * Set a direct damage effect
     */
    fun withDirectDamageEffect(damage: Int): TacticCardBuilder {
        this.effect = DirectDamageEffect(damage)
        this.cardType = TacticCardType.DIRECT_DAMAGE
        this.targetType = TargetType.ENEMY
        if (description.isEmpty()) {
            this.description = "Deal $damage damage to a target"
        }
        return this
    }

    /**
     * Set an area damage effect
     */
    fun withAreaDamageEffect(damage: Int, radius: Int = 1): TacticCardBuilder {
        this.effect = AreaDamageEffect(damage, radius)
        this.cardType = TacticCardType.AREA_EFFECT
        this.targetType = TargetType.BOARD
        if (description.isEmpty()) {
            val areaSize = radius * 2 + 1
            this.description = "Deal $damage damage to all units in a ${areaSize}x${areaSize} area"
        }
        return this
    }

    /**
     * Set a healing effect
     */
    fun withHealingEffect(amount: Int): TacticCardBuilder {
        this.effect = SingleTargetHealingEffect(amount)
        this.cardType = TacticCardType.BUFF
        this.targetType = TargetType.FRIENDLY
        if (description.isEmpty()) {
            this.description = "Restore $amount health to a friendly unit"
        }
        return this
    }

    /**
     * Set an area healing effect
     */
    fun withAreaHealingEffect(amount: Int, radius: Int = 1): TacticCardBuilder {
        this.effect = AreaHealingEffect(amount, radius)
        this.cardType = TacticCardType.BUFF
        this.targetType = TargetType.BOARD
        if (description.isEmpty()) {
            val areaSize = radius * 2 + 1
            this.description = "Restore $amount health to all friendly units in a ${areaSize}x${areaSize} area"
        }
        return this
    }

    /**
     * Set an attack buff effect
     */
    fun withAttackBuffEffect(amount: Int, duration: Int = 1): TacticCardBuilder {
        this.effect = AttackBuffEffect(amount, duration)
        this.cardType = TacticCardType.BUFF
        this.targetType = TargetType.FRIENDLY
        if (description.isEmpty()) {
            val durationText = if (duration > 1) "$duration turns" else "this turn"
            this.description = "Increase a unit's attack by $amount for $durationText"
        }
        return this
    }

    /**
     * Set a card draw effect
     */
    fun withCardDrawEffect(count: Int): TacticCardBuilder {
        this.effect = DrawCardsEffect(count)
        this.cardType = TacticCardType.SPECIAL
        this.targetType = TargetType.NONE
        if (description.isEmpty()) {
            val plural = if (count > 1) "s" else ""
            this.description = "Draw $count card$plural from your deck"
        }
        return this
    }

    /**
     * Set a charge effect (allow a unit to attack immediately)
     */
    fun withChargeEffect(): TacticCardBuilder {
        this.effect = GrantChargeEffect()
        this.cardType = TacticCardType.BUFF
        this.targetType = TargetType.FRIENDLY
        if (description.isEmpty()) {
            this.description = "Allow a unit to attack immediately"
        }
        return this
    }

    /**
     * Set a taunt effect (force enemies to attack this unit)
     */
    fun withTauntEffect(): TacticCardBuilder {
        this.effect = GrantTauntEffect()
        this.cardType = TacticCardType.BUFF
        this.targetType = TargetType.FRIENDLY
        if (description.isEmpty()) {
            this.description = "Force enemies to attack this unit if within range"
        }
        return this
    }

    /**
     * Set a movement refresh effect (allow a unit to move again)
     */
    fun withMovementRefreshEffect(): TacticCardBuilder {
        this.effect = RefreshMovementEffect()
        this.cardType = TacticCardType.BUFF
        this.targetType = TargetType.FRIENDLY
        if (description.isEmpty()) {
            this.description = "Allow a unit to move again this turn"
        }
        return this
    }
    /**
     * Build the TacticCard instance
     */
    fun build(): TacticCard {
        require(id != 0) { "Card ID is required" }
        require(name.isNotEmpty()) { "Card name is required" }
        require(description.isNotEmpty()) { "Card description is required" }
        require(manaCost >= 0) { "Mana cost must be non-negative" }
        require(effect != null) { "Card effect is required" }

        // Convert the TacticEffect to a function for the TacticCard
        val effectFunction = { player: Player, gameManager: GameManager, targetPosition: Int? ->
            effect!!.apply(player, gameManager, targetPosition)
        }

        return TacticCard(
            id = id,
            name = name,
            description = description,
            manaCost = manaCost,
            imagePath = imagePath.ifEmpty { "tactic_${cardType.name.lowercase()}" },
            cardType = cardType,
            targetType = targetType,
            effect = effectFunction
        )
    }

    companion object {
        /**
         * Create a fireball card (direct damage)
         */
        fun createFireball(id: Int, damage: Int = 4, manaCost: Int = 3): TacticCard {
            return TacticCardBuilder()
                .withId(id)
                .withName("Fireball")
                .withManaCost(manaCost)
                .withImagePath("tactic_fireball")
                .withDirectDamageEffect(damage)
                .build()
        }

        /**
         * Create a healing potion card
         */
        fun createHealingPotion(id: Int, healAmount: Int = 5, manaCost: Int = 2): TacticCard {
            return TacticCardBuilder()
                .withId(id)
                .withName("Healing Potion")
                .withManaCost(manaCost)
                .withImagePath("tactic_healing")
                .withHealingEffect(healAmount)
                .build()
        }

        /**
         * Create an explosion card (area damage)
         */
        fun createExplosion(id: Int, damage: Int = 3, radius: Int = 1, manaCost: Int = 6): TacticCard {
            return TacticCardBuilder()
                .withId(id)
                .withName("Explosion")
                .withManaCost(manaCost)
                .withImagePath("tactic_explosion")
                .withAreaDamageEffect(damage, radius)
                .build()
        }

        /**
         * Create a battle rage card (attack buff)
         */
        fun createBattleRage(id: Int, attackBoost: Int = 3, duration: Int = 2, manaCost: Int = 4): TacticCard {
            return TacticCardBuilder()
                .withId(id)
                .withName("Battle Rage")
                .withManaCost(manaCost)
                .withImagePath("tactic_rage")
                .withAttackBuffEffect(attackBoost, duration)
                .build()
        }

        /**
         * Create a card draw spell
         */
        fun createArcaneIntellect(id: Int, cardsToDraw: Int = 2, manaCost: Int = 3): TacticCard {
            return TacticCardBuilder()
                .withId(id)
                .withName("Arcane Intellect")
                .withManaCost(manaCost)
                .withImagePath("tactic_intellect")
                .withCardDrawEffect(cardsToDraw)
                .build()
        }

        /**
         * Create a charge card
         */
        fun createCharge(id: Int, manaCost: Int = 2): TacticCard {
            return TacticCardBuilder()
                .withId(id)
                .withName("Charge")
                .withManaCost(manaCost)
                .withImagePath("tactic_charge")
                .withChargeEffect()
                .build()
        }
        
    }
}