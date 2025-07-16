package com.example.mtglifetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the global settings for the game.
 *
 * This data class is an entity that maps to the `game_settings` table. It holds
 * application-wide settings like the number of players in the active game layout and
 * the default starting life total. There is only ever one row in this table,
 * identified by a fixed primary key of 1.
 *
 * @property id The primary key for the settings entry, fixed at 1.
 * @property playerCount The number of players for the currently active game layout (e.g., 2, 3, 4).
 * @property startingLife The default life total players are set to when a game is started or reset.
 */
@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey
    val id: Int = 1,
    val playerCount: Int = 2,
    val startingLife: Int = 40
)