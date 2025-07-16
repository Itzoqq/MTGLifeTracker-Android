package com.example.mtglifetracker.model

import androidx.room.Entity

/**
 * Represents the amount of commander damage one player has dealt to another.
 *
 * This data class is an entity that maps to the `commander_damage` table in the Room database.
 * Each instance is uniquely identified by a composite primary key consisting of the game size,
 * the source player, and the target player.
 *
 * @property gameSize The number of players in the game this damage entry belongs to (e.g., 2, 4).
 * @property sourcePlayerIndex The 0-based index of the player *dealing* the commander damage.
 * @property targetPlayerIndex The 0-based index of the player *receiving* the commander damage.
 * @property damage The total amount of commander damage dealt from the source to the target.
 */
@Entity(tableName = "commander_damage", primaryKeys = ["gameSize", "sourcePlayerIndex", "targetPlayerIndex"])
data class CommanderDamage(
    val gameSize: Int,
    val sourcePlayerIndex: Int,
    val targetPlayerIndex: Int,
    val damage: Int = 0
)