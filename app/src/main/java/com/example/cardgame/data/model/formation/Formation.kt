package com.example.cardgame.data.model.formation

class Formation(
    val name: String,
    val description: String,
    val unitPositions: List<Int>,
    val effects: List<FormationEffect>
)