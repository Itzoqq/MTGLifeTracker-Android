package com.example.mtglifetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile

// Change version from 5 to 6
@Database(entities = [Player::class, GameSettings::class, Profile::class], version = 6, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun profileDao(): ProfileDao
}