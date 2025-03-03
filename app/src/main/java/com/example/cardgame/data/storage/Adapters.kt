package com.example.cardgame.data.storage

import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.data.model.abilities.ChargeAbility
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CardTypeAdapter : JsonSerializer<Card>, JsonDeserializer<Card> {
    override fun serialize(src: Card, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", src.id)
        jsonObject.addProperty("name", src.name)
        jsonObject.addProperty("description", src.description)
        jsonObject.addProperty("manaCost", src.manaCost)
        jsonObject.addProperty("imagePath", src.imagePath)

        when (src) {
            is UnitCard -> {
                jsonObject.addProperty("type", "unit")
                jsonObject.addProperty("attack", src.attack)
                jsonObject.addProperty("health", src.health)
                jsonObject.addProperty("maxHealth", src.maxHealth)
                jsonObject.addProperty("unitType", src.unitType.name)
                jsonObject.addProperty("unitEra", src.unitEra.name)
                jsonObject.add("abilities", context.serialize(src.abilities))
                jsonObject.addProperty("hasCharge", src.hasCharge)
                jsonObject.addProperty("hasTaunt", src.hasTaunt)
            }
            is TacticCard -> {
                jsonObject.addProperty("type", "tactic")
                // We can't directly serialize a function, so store effect information differently
                jsonObject.addProperty("effectType", "direct_damage") // Example effect type
                jsonObject.addProperty("effectValue", 3) // Example effect value
            }
        }

        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Card {
        val jsonObject = json.asJsonObject
        val id = jsonObject.get("id").asInt
        val name = jsonObject.get("name").asString
        val description = jsonObject.get("description").asString
        val manaCost = jsonObject.get("manaCost").asInt
        val imagePath = jsonObject.get("imagePath").asString

        return when (jsonObject.get("type").asString) {
            "unit" -> {
                val attack = jsonObject.get("attack").asInt
                val health = jsonObject.get("health").asInt
                val maxHealth = jsonObject.get("maxHealth").asInt
                val unitType = UnitType.valueOf(jsonObject.get("unitType").asString)
                val unitEra = UnitEra.valueOf(jsonObject.get("unitEra").asString)

                val abilitiesType = object : TypeToken<List<Ability>>() {}.type
                val abilities = context.deserialize<List<Ability>>(
                    jsonObject.get("abilities"),
                    abilitiesType
                ) ?: emptyList()

                val hasCharge = jsonObject.get("hasCharge")?.asBoolean ?: false
                val hasTaunt = jsonObject.get("hasTaunt")?.asBoolean ?: false

                UnitCard(
                    id = id,
                    name = name,
                    description = description,
                    manaCost = manaCost,
                    imagePath = imagePath,
                    attack = attack,
                    health = health,
                    maxHealth = maxHealth,
                    unitType = unitType,
                    unitEra = unitEra,
                    abilities = abilities,
                    hasCharge = hasCharge,
                    hasTaunt = hasTaunt
                )
            }
            "tactic" -> {
                // Parse effect information
                val effectType = jsonObject.get("effectType")?.asString ?: "none"
                val effectValue = jsonObject.get("effectValue")?.asInt ?: 0

                // Create a function based on the effect type
                val effect: (Player, GameManager, Int?) -> Boolean = when (effectType) {
                    "direct_damage" -> { player, gameManager, targetPosition ->
                        if (targetPosition != null) {
                            val opponent = gameManager.getOpponentOf(player)
                            val targetUnit = opponent?.board?.getUnitAt(targetPosition)
                            if (targetUnit != null) {
                                targetUnit.takeDamage(effectValue)
                                gameManager.checkForDestroyedUnits()
                                true
                            } else false
                        } else false
                    }
                    "heal" -> { player, gameManager, targetPosition ->
                        if (targetPosition != null) {
                            val targetUnit = player.board.getUnitAt(targetPosition)
                            if (targetUnit != null) {
                                targetUnit.heal(effectValue)
                                true
                            } else false
                        } else false
                    }
                    else -> { _, _, _ -> true }
                }

                TacticCard(
                    id = id,
                    name = name,
                    description = description,
                    manaCost = manaCost,
                    imagePath = imagePath,
                    effect = effect
                )
            }
            else -> throw IllegalArgumentException("Unknown card type")
        }
    }
}

/**
 * Custom type adapter for Ability class and its subclasses
 */
class AbilityTypeAdapter : JsonSerializer<Ability>, JsonDeserializer<Ability> {
    override fun serialize(src: Ability, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()

        when (src) {
            is ChargeAbility -> jsonObject.addProperty("abilityType", "charge")
       //     is TauntAbility -> jsonObject.addProperty("abilityType", "taunt")
            // Add other ability types as needed
            else -> jsonObject.addProperty("abilityType", "unknown")
        }

        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Ability {
        val jsonObject = json.asJsonObject
        val abilityType = jsonObject.get("abilityType").asString

        return when (abilityType) {
            "charge" -> ChargeAbility()
        //    "taunt" -> TauntAbility()
            // Add other ability types as needed
            else -> object : Ability {
                override val name = "Unknown"
                override val description = "Unknown ability"
                override fun apply(unit: UnitCard, gameManager: GameManager) {}
            }
        }
    }
}