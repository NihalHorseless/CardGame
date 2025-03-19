package com.example.cardgame.data.enum

enum class InteractionMode {
    DEFAULT,       // Normal selection mode
    CARD_TARGETING, // Selecting a target for a card
    UNIT_ATTACKING, // Unit is attacking
    UNIT_MOVING     // Unit is moving
}