package com.example.mtglifetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile

@Database(entities = [Player::class, GameSettings::class, Profile::class, CommanderDamage::class], version = 9, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun profileDao(): ProfileDao
    abstract fun commanderDamageDao(): CommanderDamageDao

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE profiles_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nickname` TEXT NOT NULL, `color` TEXT)")
                db.execSQL("INSERT INTO profiles_new (id, nickname, color) SELECT id, nickname, color FROM profiles")
                db.execSQL("DROP TABLE profiles")
                db.execSQL("ALTER TABLE profiles_new RENAME TO profiles")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `commander_damage` (`gameSize` INTEGER NOT NULL, `sourcePlayerIndex` INTEGER NOT NULL, `targetPlayerIndex` INTEGER NOT NULL, `damage` INTEGER NOT NULL, PRIMARY KEY(`gameSize`, `sourcePlayerIndex`, `targetPlayerIndex`))")
            }
        }
    }
}