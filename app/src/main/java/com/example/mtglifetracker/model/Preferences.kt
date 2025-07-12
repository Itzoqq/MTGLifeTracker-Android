package com.example.mtglifetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class Preferences(
    @PrimaryKey
    val id: Int = 1,
    val deduceCommanderDamage: Boolean = true
)