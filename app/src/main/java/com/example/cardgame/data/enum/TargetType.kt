package com.example.cardgame.data.enum

enum class TargetType {
    NONE,       // No target needed
    FRIENDLY,   // Target a friendly unit
    ENEMY,      // Target an enemy unit
    ANY,        // Target any unit
    BOARD       // Target a position on the board
}