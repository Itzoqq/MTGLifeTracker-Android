package com.example.mtglifetracker.model

/**
 * Data class representing a single player's state.
 * This class is a pure data holder.
 *
 * @property life The current life total of the player. Defaults to 40.
 * @property name The name of the player.
 */
data class Player(
    val life: Int = 40,
    val name: String = "Player"
)