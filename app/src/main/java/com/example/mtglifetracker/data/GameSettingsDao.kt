package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtglifetracker.model.GameSettings
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `game_settings` table.
 *
 * This interface defines the database operations for the [GameSettings] entity.
 * It provides methods to save and retrieve application-wide settings, such as
 * the default player count and starting life total. Since there is only ever one
 * row of settings (with a fixed primary key of 1), the operations are straightforward.
 *
 * Logging for these database calls is handled within the [GameRepository], which is
 * the sole caller of this DAO.
 */
@Dao
interface GameSettingsDao {

    /**
     * Saves or updates the application's game settings.
     *
     * This method uses `OnConflictStrategy.REPLACE`, which means if a settings entry
     * with the primary key of 1 already exists, it will be completely replaced with the
     * new settings object provided. If it doesn't exist, a new one will be inserted.
     * This ensures there is always a single, up-to-date row for settings.
     *
     * @param settings The [GameSettings] object to be saved to the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettings)

    /**
     * Retrieves the application's game settings as a reactive flow.
     *
     * This method returns a [Flow] that will automatically emit the current [GameSettings]
     * object whenever it is changed in the database. This allows the repository and, by extension,
     * the UI to reactively update when settings are modified.
     * The query specifically looks for the entry with `id = 1`.
     *
     * @return A [Flow] that emits the [GameSettings] object, or null if no settings
     * have been saved yet.
     */
    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<GameSettings?>
}