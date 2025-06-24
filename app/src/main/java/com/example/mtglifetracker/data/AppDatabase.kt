package com.example.mtglifetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile

@Database(entities = [Player::class, GameSettings::class, Profile::class], version = 8, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun profileDao(): ProfileDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_settings ADD COLUMN startingLife INTEGER NOT NULL DEFAULT 40")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
            }
        }

        // This migration now tests dropping a column, which requires no refactoring.
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new table without the 'isDefault' column
                db.execSQL("CREATE TABLE profiles_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nickname` TEXT NOT NULL, `color` TEXT)")
                // Copy the data from the old table to the new one
                db.execSQL("INSERT INTO profiles_new (id, nickname, color) SELECT id, nickname, color FROM profiles")
                // Remove the old table
                db.execSQL("DROP TABLE profiles")
                // Rename the new table to the original name
                db.execSQL("ALTER TABLE profiles_new RENAME TO profiles")
            }
        }
    }
}