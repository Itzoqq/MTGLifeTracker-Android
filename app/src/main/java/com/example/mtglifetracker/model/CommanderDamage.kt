package com.example.mtglifetracker.model

import androidx.room.Entity

@Entity(tableName = "commander_damage", primaryKeys = ["gameSize", "sourcePlayerIndex", "targetPlayerIndex"])
data class CommanderDamage(
    val gameSize: Int,
    val sourcePlayerIndex: Int, // The player dealing the damage
    val targetPlayerIndex: Int, // The player receiving the damage
    val damage: Int = 0
)