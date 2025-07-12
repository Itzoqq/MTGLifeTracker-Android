package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtglifetracker.model.Preferences
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: Preferences)

    @Query("SELECT * FROM preferences WHERE id = 1")
    fun getPreferences(): Flow<Preferences?>
}