package com.example.mtglifetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a single player's state.
 * This class is a pure data holder.
 *
 * @property life The current life total of the player. Defaults to 40.
 * @property name The name of the player.
 */
@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val life: Int = 40,
    val name: String = "Player"
)