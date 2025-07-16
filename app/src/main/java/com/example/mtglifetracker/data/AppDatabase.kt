package com.example.mtglifetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger

/**
 * The main database class for the application, built on Room.
 *
 * This abstract class serves as the central access point for all persisted data. It defines the
 * list of entities (tables), the database version, and provides abstract methods for accessing
 * each Data Access Object (DAO). It also contains the migration strategies required to update
 * the database schema between versions without losing user data.
 *
 * The `version` of the database schema must be incremented each time the schema is changed.
 * The `entities` are the list of data classes that are mapped to tables in the database.
 * When `exportSchema` is true, Room exports the database schema into a JSON file, which is
 * highly recommended for version control and migration testing.
 */
@Database(
    entities = [Player::class, GameSettings::class, Profile::class, CommanderDamage::class, Preferences::class],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // Abstract methods for Room to implement, providing access to each DAO.

    /**
     * @return The Data Access Object for [Player] entities.
     */
    abstract fun playerDao(): PlayerDao

    /**
     * @return The Data Access Object for the [GameSettings] entity.
     */
    abstract fun gameSettingsDao(): GameSettingsDao

    /**
     * @return The Data Access Object for [Profile] entities.
     */
    abstract fun profileDao(): ProfileDao

    /**
     * @return The Data Access Object for [CommanderDamage] entities.
     */
    abstract fun commanderDamageDao(): CommanderDamageDao

    /**
     * @return The Data Access Object for the [Preferences] entity.
     */
    abstract fun preferencesDao(): PreferencesDao

    /**
     * A companion object to hold static members, primarily the database migration definitions.
     * Each migration provides a path for Room to update the database from an older version
     * to a newer one.
     */
    companion object {
        /**
         * Migration from database version 5 to 6.
         * This migration adds the `startingLife` column to the `game_settings` table
         * to allow users to customize the starting life total.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Logger.i("DB Migration: Running migration from version 5 to 6.")
                db.execSQL("ALTER TABLE game_settings ADD COLUMN startingLife INTEGER NOT NULL DEFAULT 40")
                Logger.d("DB Migration: 5->6 - Added 'startingLife' column to 'game_settings' table.")
            }
        }

        /**
         * Migration from database version 6 to 7.
         * This migration added a now-removed `isDefault` column. It is kept here for historical
         * integrity to allow users on older versions to migrate correctly through the chain.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Logger.i("DB Migration: Running migration from version 6 to 7.")
                db.execSQL("ALTER TABLE profiles ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
                Logger.d("DB Migration: 6->7 - Added 'isDefault' column to 'profiles' table.")
            }
        }

        /**
         * Migration from database version 7 to 8.
         * This migration removes the `isDefault` column from the `profiles` table as it was
         * deemed unnecessary. This is achieved by creating a new table without the column,
         * copying the data, and then dropping the old table.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Logger.i("DB Migration: Running migration from version 7 to 8.")
                Logger.d("DB Migration: 7->8 - Creating new profiles table 'profiles_new'.")
                db.execSQL("CREATE TABLE profiles_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nickname` TEXT NOT NULL, `color` TEXT)")
                Logger.d("DB Migration: 7->8 - Copying data from old 'profiles' to 'profiles_new'.")
                db.execSQL("INSERT INTO profiles_new (id, nickname, color) SELECT id, nickname, color FROM profiles")
                Logger.d("DB Migration: 7->8 - Dropping old 'profiles' table.")
                db.execSQL("DROP TABLE profiles")
                Logger.d("DB Migration: 7->8 - Renaming 'profiles_new' to 'profiles'.")
                db.execSQL("ALTER TABLE profiles_new RENAME TO profiles")
            }
        }

        /**
         * Migration from database version 8 to 9.
         * This migration introduces the `commander_damage` table to store commander damage
         * totals between players for each game size.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Logger.i("DB Migration: Running migration from version 8 to 9.")
                db.execSQL("CREATE TABLE IF NOT EXISTS `commander_damage` (`gameSize` INTEGER NOT NULL, `sourcePlayerIndex` INTEGER NOT NULL, `targetPlayerIndex` INTEGER NOT NULL, `damage` INTEGER NOT NULL, PRIMARY KEY(`gameSize`, `sourcePlayerIndex`, `targetPlayerIndex`))")
                Logger.d("DB Migration: 8->9 - Created 'commander_damage' table.")
            }
        }

        /**
         * Migration from database version 9 to 10.
         * This migration adds the `preferences` table to store user-specific app settings,
         * starting with the option to automatically deduce commander damage from life totals.
         * It also inserts a default value for this preference.
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Logger.i("DB Migration: Running migration from version 9 to 10.")
                db.execSQL("CREATE TABLE IF NOT EXISTS `preferences` (`id` INTEGER NOT NULL, `deduceCommanderDamage` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                Logger.d("DB Migration: 9->10 - Created 'preferences' table.")
                db.execSQL("INSERT INTO preferences (id, deduceCommanderDamage) VALUES (1, 1)")
                Logger.d("DB Migration: 9->10 - Inserted default preference values.")
            }
        }
    }
}