package com.example.mtglifetracker.model

import androidx.room.Entity

/**
 * Data class representing a single player's state.
 * This class is a pure data holder.
 *
 * A player is now uniquely identified by the combination of the game size
 * and their index within that game.
 *
 * @property gameSize The number of players in the game this player belongs to (e.g., 2, 4).
 * @property playerIndex The 0-based index of this player within their game.
 * @property life The current life total of the player.
 * @property name The name of the player.
 * @property profileId The ID of the profile associated with this player.
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