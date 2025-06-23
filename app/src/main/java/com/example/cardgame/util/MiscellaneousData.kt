package com.example.cardgame.util

import com.example.cardgame.R
import com.example.cardgame.data.model.msc.GameMechanic

object MiscellaneousData {
    val mechanics = listOf(
        GameMechanic(
            id = "counter_system",
            title = "Counter System",
            description = "Units have strengths and weaknesses against other unit types. When a unit attacks a type it counters, it deals double damage!\n\n" +
                    "• Cavalry deals double damage to Missile and Artillery units\n" +
                    "• Infantry deals double damage to Cavalry\n" +
                    "• Artillery deals double damage to fortifications",
            iconResId = R.drawable.counter_icon,
            example = "A Cavalry unit with 3 attack will deal 6 damage to a Missile unit.",
            gifResId = R.drawable.counter_mechanic
        ),
        GameMechanic(
            id = "movement",
            title = "Movement",
            description = "Different unit types have different movement capabilities:\n\n" +
                    "• Cavalry units can move up to 2 spaces per turn\n" +
                    "• All other units can move 1 space per turn\n\n" +
                    "Select a unit, then select a blue-highlighted destination to move.",
            iconResId = R.drawable.wind_svgrepo_com,
            gifResId = R.drawable.movement_mechanics
        ),
        GameMechanic(
            id = "attack_range",
            title = "Attack Range",
            description = "Units have different attack ranges:\n\n" +
                    "• Melee units (Infantry, Cavalry) must be adjacent to attack\n" +
                    "• Missile and Musket units can attack up to 2 spaces away\n" +
                    "• Artillery units can attack up to 3 spaces away, but not adjacent enemies\n\n" +
                    "Select a unit, then select a red-highlighted target to attack.",
            iconResId = R.drawable.range_menu,
            gifResId = R.drawable.range_mechanics
        ),
        GameMechanic(
            id = "fortifications",
            title = "Fortifications",
            description = "Fortifications are static structures that provide defensive capabilities:\n\n" +
                    "• Walls cannot attack but provide defense\n" +
                    "• Towers can attack enemy units up to 2 spaces away\n\n" +
                    "Fortifications are deployed like units and remain in place.",
            iconResId = R.drawable.fort_system,
            gifResId = R.drawable.fortifications_mechanic
        ),
        GameMechanic(
            id = "win_conditions",
            title = "How to Win",
            description = "Main goal of the game is to hit the enemy General portrait until it's health goes to 0.\n\n" +
                    "In order to attack the enemy General you have to move your units to enemy portrait's row.",
            iconResId = R.drawable.eagle_standard,
            gifResId = R.drawable.win_condition_mechanics
        ),
        GameMechanic(
            id = "charge",
            title = "Charge",
            description = "Units with Charge ability can attack on the same turn they are deployed.\n\n" +
                    "Normal units must wait until the next turn after deployment to attack.",
            iconResId = R.drawable.charge_icon,
            gifResId = R.drawable.charge_mechanic
        ),
        GameMechanic(
            id = "bayonet",
            title = "Bayonet",
            description = "Musket units can attach a bayonet to transform into Infantry units.\n\n" +
                    "When selected, musket units show a bayonet button that can be clicked to transform them. The unit cannot move or attack on the turn it transforms.",
            iconResId = R.drawable.bayonet_menu,
            gifResId = R.drawable.bayonet_mechanic
        ),
        GameMechanic(
            id = "tactic_cards",
            title = "Tactic Cards",
            description = "Tactic cards provide special abilities:\n\n" +
                    "• Direct Damage - Deal damage to a target\n" +
                    "• Area Effect - Deal damage in an area\n" +
                    "• Buff - Enhance friendly units\n" +
                    "• Debuff - Weaken enemy units\n" +
                    "• Special - Various effects like drawing cards\n\n" +
                    "Select a tactic card from your hand, then select a target to apply the effect.",
            iconResId = R.drawable.buff_effect,
            gifResId = R.drawable.tactical_card_mechanic
        ),
        GameMechanic(
            id = "deck_cards",
            title = "Decks",
            description = "Decks consist 30 cards\n\n" +
                    "• Every turn each player draws a card\n" +
                    "• Cards cost blood drops (the mana of the game)\n" +
                    "• Players who run out of cards start taking damage each turn\n" ,
            iconResId = R.drawable.card_back,
            gifResId = R.drawable.cards_mechanics
        )
    )
}