package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtglifetracker.model.Preferences
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `preferences` table.
 *
 * This interface defines the database operations for the [Preferences] entity.
 * It manages the storage and retrieval of user-specific settings, such as whether
 * to automatically deduct life when commander damage is dealt. Like the [GameSettingsDao],
 * it operates on a single row of data with a fixed primary key of 1.
 *
 * Logging for these database calls is handled within the [GameRepository].
 */
@Dao
interface PreferencesDao {

    /**
     * Saves or updates the user's application preferences.
     *
     * This method uses `OnConflictStrategy.REPLACE` to ensure that if a preferences
     * entry already exists (with primary key 1), it is overwritten with the new
     * object. If no entry exists, a new one is inserted. This guarantees a single,
     * consistent record for user preferences.
     *
     * @param preferences The [Preferences] object to be saved.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: Preferences)

    /**
     * Retrieves the user's application preferences as a reactive flow.
     *
     * This method returns a [Flow] that will emit the current [Preferences] object
     * whenever it's changed in the database. This allows the application to react
     * dynamically to changes in user settings.
     *
     * @return A [Flow] that emits the [Preferences] object, or null if no
     * preferences have been saved yet.
     */
    @Query("SELECT * FROM preferences WHERE id = 1")
    fun getPreferences(): Flow<Preferences?>
}