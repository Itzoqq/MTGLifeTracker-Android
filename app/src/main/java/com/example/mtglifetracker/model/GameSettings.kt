package com.example.mtglifetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey
    val id: Int = 1,
    val playerCount: Int = 2,
    val startingLife: Int = 40 // New field
)