package io.github.nihalhorseless.eternalglory.data.model.msc

data class GameMechanic(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val example: String = "",
    val gifResId: Int? = null  // Add Gif resource ID
)
