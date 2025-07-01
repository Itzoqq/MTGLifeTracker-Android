package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtglifetracker.model.CommanderDamage
import kotlinx.coroutines.flow.Flow

@Dao
interface CommanderDamageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(damages: List<CommanderDamage>)

    @Query("SELECT * FROM commander_damage WHERE gameSize = :gameSize AND targetPlayerIndex = :targetPlayerIndex")
    fun getCommanderDamageForPlayer(gameSize: Int, targetPlayerIndex: Int): Flow<List<CommanderDamage>>

    @Query("UPDATE commander_damage SET damage = damage + 1 WHERE gameSize = :gameSize AND sourcePlayerIndex = :sourcePlayerIndex AND targetPlayerIndex = :targetPlayerIndex")
    suspend fun incrementCommanderDamage(gameSize: Int, sourcePlayerIndex: Int, targetPlayerIndex: Int)

    @Query("UPDATE commander_damage SET damage = MAX(0, damage - 1) WHERE gameSize = :gameSize AND sourcePlayerIndex = :sourcePlayerIndex AND targetPlayerIndex = :targetPlayerIndex")
    suspend fun decrementCommanderDamage(gameSize: Int, sourcePlayerIndex: Int, targetPlayerIndex: Int)

    @Query("SELECT COUNT(*) FROM commander_damage WHERE gameSize = :gameSize")
    suspend fun getDamageEntryCountForGame(gameSize: Int): Int

    @Query("DELETE FROM commander_damage WHERE gameSize = :gameSize")
    suspend fun deleteCommanderDamageForGame(gameSize: Int)

    // *** THE FIX IS HERE ***
    @Query("DELETE FROM commander_damage")
    suspend fun deleteAll()
}