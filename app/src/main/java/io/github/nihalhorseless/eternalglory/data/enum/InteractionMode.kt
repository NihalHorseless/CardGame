package io.github.nihalhorseless.eternalglory.data.enum

enum class InteractionMode {
    DEFAULT,       // Normal selection mode
    CARD_TARGETING, // Selecting a target for a tactic card
    UNIT_ATTACKING, // Unit is attacking
    UNIT_MOVING,    // Unit is moving
    DEPLOY          // New mode for deploying units/fortifications
}