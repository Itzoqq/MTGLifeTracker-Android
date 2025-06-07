package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mtglifetracker.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<Player>)

    @Update
    suspend fun updatePlayer(player: Player)

    @Query("SELECT * FROM players WHERE gameSize = :gameSize ORDER BY playerIndex ASC")
    fun getPlayers(gameSize: Int): Flow<List<Player>>

    @Query("DELETE FROM players WHERE gameSize = :gameSize")
    suspend fun deletePlayersForGame(gameSize: Int)

    @Query("DELETE FROM players")
    suspend fun deleteAll()
}