package com.example.mtglifetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents user-configurable application preferences.
 *
 * This data class is an entity that maps to the `preferences` table. It stores settings
 * that the user can change to alter the application's behavior. Similar to [GameSettings],
 * this table will only ever contain a single row with a fixed primary key of 1.
 *
 * @property id The primary key for the preferences entry, fixed at 1.
 * @property deduceCommanderDamage A boolean indicating whether dealing commander damage
 * should automatically decrease the target player's life total.
 */
@Entity(tableName = "preferences")
data class Preferences(
    @PrimaryKey
    val id: Int = 1,
    val deduceCommanderDamage: Boolean = true
)