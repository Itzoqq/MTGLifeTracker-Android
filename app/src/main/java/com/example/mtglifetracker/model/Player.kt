package com.example.mtglifetracker.model

import androidx.room.Entity

/**
 * Represents the state of a single player within a specific game instance.
 *
 * This data class is an entity that maps to the `players` table in the database.
 * A player is uniquely identified by the combination of the game size and their index
 * within that game. This allows the application to store separate game states for
 * 2-player games, 3-player games, etc., simultaneously.
 *
 * @property gameSize The number of players in the game this player instance belongs to (e.g., 2, 4).
 * @property playerIndex The 0-based index of this player within their specific game.
 * @property life The current life total of the player.
 * @property name The display name of the player. Defaults to "Player X" but can be
 * updated by assigning a [Profile].
 * @property profileId The ID of the [Profile] currently assigned to this player. This is nullable,
 * indicating that a player may not have a profile assigned.
 * @property color A string representing the hex color code associated with the assigned profile.
 * This is stored directly on the player for quick UI rendering and to avoid database joins.
 */
@Entity(tableName = "players", primaryKeys = ["gameSize", "playerIndex"])
data class Player(
    val gameSize: Int,
    val playerIndex: Int,
    val life: Int,
    val name: String = "Player",
    val profileId: Long? = null,
    val color: String? = null
)