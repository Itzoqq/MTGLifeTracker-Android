package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtglifetracker.model.GameSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettings)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<GameSettings?>
}