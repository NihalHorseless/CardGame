package com.example.cardgame.data.storage

import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.enum.TargetType
import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.data.model.abilities.ChargeAbility
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player
import com.example.cardgame.util.CardTestData.samplePlayer
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
            is FortificationCard -> {
                // New: Fortification card serialization
                jsonObject.addProperty("type", "fortification")
                jsonObject.addProperty("attack", src.attack)
                jsonObject.addProperty("health", src.health)
                jsonObject.addProperty("maxHealth", src.maxHealth)
                jsonObject.addProperty("fortType", src.fortType.name)
                jsonObject.addProperty("canAttackThisTurn", src.canAttackThisTurn)
            }
            is TacticCard -> {
                // Serialize TacticCard-specific properties
                jsonObject.addProperty("type", "tactic")
                jsonObject.addProperty("cardType", src.cardType.name)
                jsonObject.addProperty("targetType", src.targetType.name)

                // We can't directly serialize the effect function, but we can add a placeholder
                // The actual effects will be recreated during deserialization
                val effectsArray = context.serialize(
                    arrayOf(mapOf("effectType" to "placeholder", "effectValue" to 0))
                )
                jsonObject.add("effects", effectsArray)
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
                    abilities = abilities.toMutableList(),
                    hasCharge = hasCharge,
                    hasTaunt = hasTaunt
                )
            }
            "fortification" -> {
                // New: Fortification card deserialization
                val attack = jsonObject.get("attack").asInt
                val health = jsonObject.get("health").asInt
                val maxHealth = jsonObject.get("maxHealth").asInt

                // Parse the fortification type
                val fortTypeStr = jsonObject.get("fortType").asString
                val fortType = FortificationType.valueOf(fortTypeStr)

                FortificationCard(
                    id = id,
                    name = name,
                    description = description,
                    manaCost = manaCost,
                    imagePath = imagePath,
                    attack = attack,
                    health = health,
                    maxHealth = maxHealth,
                    fortType = fortType,
                    canAttackThisTurn = false
                )
            }
            "tactic" -> {
                // For tactic cards, use the dedicated TacticCardDeserializer
                // This handles the separation of concerns correctly
                context.deserialize<TacticCard>(json, TacticCard::class.java)
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