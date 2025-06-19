package com.example.mtglifetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile

// Change version from 5 to 6
@Database(entities = [Player::class, GameSettings::class, Profile::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mtg_database"
                )
                    // This will clear the database on upgrade. This is fine for development.
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}