package com.example.cardgame.data.storage

import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.enum.TargetType
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.effect.TacticEffect
import com.example.cardgame.data.model.effect.TacticEffectFactory
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * JSON deserializer for TacticCard objects.
 * This class handles converting JSON data into TacticCard instances,
 * creating the appropriate effects based on the JSON structure.
 */
class TacticCardDeserializer : JsonDeserializer<TacticCard> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TacticCard {
        val jsonObject = json.asJsonObject

        // Parse basic card properties
        val id = jsonObject.get("id").asInt
        val name = jsonObject.get("name").asString
        val description = jsonObject.get("description").asString
        val manaCost = jsonObject.get("manaCost").asInt
        val imagePath = jsonObject.get("imagePath").asString

        // Parse card type and target type enums
        val cardType = TacticCardType.valueOf(jsonObject.get("cardType").asString)
        val targetType = TargetType.valueOf(jsonObject.get("targetType").asString)

        // Parse effects
        val effectsArray = jsonObject.getAsJsonArray("effects")
        val effects = mutableListOf<TacticEffect>()

        // Process each effect in the effects array
        for (effectElement in effectsArray) {
            val effectObj = effectElement.asJsonObject
            val effectType = effectObj.get("effectType").asString
            val effectValue = effectObj.get("effectValue").asInt

            // Check for optional properties
            val duration = if (effectObj.has("duration")) {
                effectObj.get("duration").asInt
            } else {
                1 // Default duration
            }

            val radius = if (effectObj.has("radius")) {
                effectObj.get("radius").asInt
            } else {
                1 // Default radius for area effects
            }

            // Create the effect based on its type and properties
            val effect = when (effectType) {
                "area_damage" -> TacticEffectFactory.createEffect(
                    effectType = effectType,
                    effectValue = effectValue,
                    radius = radius
                )
                "area_healing" -> TacticEffectFactory.createEffect(
                    effectType = effectType,
                    effectValue = effectValue,
                    radius = radius
                )
                "buff_attack" -> TacticEffectFactory.createEffect(
                    effectType = effectType,
                    effectValue = effectValue,
                    duration = duration
                )
                else -> TacticEffectFactory.createEffect(
                    effectType = effectType,
                    effectValue = effectValue
                )
            }

            effects.add(effect)
        }

        // Create a compound effect if there are multiple effects
        val finalEffect = if (effects.size > 1) {
            TacticEffectFactory.createCompoundEffect(effects)
        } else {
            effects.firstOrNull() ?: TacticEffectFactory.createEffect("none", 0)
        }

        // Convert the TacticEffect to a function for the TacticCard
        val effectFunction = { player: Player, gameManager: GameManager, targetPosition: Int? ->
            finalEffect.apply(player, gameManager, targetPosition)
        }

        // Create and return the TacticCard
        return TacticCard(
            id = id,
            name = name,
            description = description,
            manaCost = manaCost,
            imagePath = imagePath,
            cardType = cardType,
            targetType = targetType,
            effect = effectFunction
        )
    }
}