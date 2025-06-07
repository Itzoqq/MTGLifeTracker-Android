package com.example.mtglifetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player

@Database(entities = [Player::class, GameSettings::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSettingsDao(): GameSettingsDao

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
                    // We've changed the DB schema, so a migration is needed.
                    // This will clear the database on upgrade.
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}