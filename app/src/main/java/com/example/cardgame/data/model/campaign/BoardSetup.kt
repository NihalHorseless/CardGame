package com.example.cardgame.data.model.campaign

data class BoardSetup(
    val unitId: Int,
    val row: Int,
    val col: Int,
    val isPlayerUnit: Boolean
)
