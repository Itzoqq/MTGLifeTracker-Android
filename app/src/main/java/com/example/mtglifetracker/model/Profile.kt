package com.example.mtglifetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user-created, savable profile.
 *
 * A profile consists of a unique nickname and an optional associated color. Users can create,
 * edit, and delete these profiles. They can then be assigned to a player in any game,
 * which will update the player's name and background color to match the profile.
 *
 * @property id The unique, auto-generated primary key for the profile.
 * @property nickname The user-defined name for the profile. This must be unique.
 * @property color An optional string representing the hex color code for the profile.
 * If null, no specific color is associated with the profile.
 */
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nickname: String,
    val color: String?
)