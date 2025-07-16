package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mtglifetracker.model.CommanderDamage
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `commander_damage` table.
 *
 * This interface defines all the necessary SQL operations for creating, reading,
 * updating, and deleting [CommanderDamage] entities in the database. Room will
 * generate the implementation for these methods at compile time.
 */
@Dao
interface CommanderDamageDao {

    /**
     * Inserts a list of [CommanderDamage] entries into the database.
     *
     * If an entry with the same primary key (a combination of gameSize, sourcePlayerIndex,
     * and targetPlayerIndex) already exists, it will be ignored. This is useful for
     * initializing damage entries for a game without creating duplicates.
     *
     * @param damages A list of [CommanderDamage] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(damages: List<CommanderDamage>)

    /**
     * Retrieves a reactive flow of all commander damage entries for a specific player
     * in a specific game size.
     *
     * This method returns a [Flow], which will automatically emit a new list of
     * [CommanderDamage] objects whenever the underlying data changes for the specified player.
     *
     * @param gameSize The number of players in the game (e.g., 2, 4).
     * @param targetPlayerIndex The index of the player who is receiving the damage.
     * @return A [Flow] emitting a list of [CommanderDamage] objects.
     */
    @Query("SELECT * FROM commander_damage WHERE gameSize = :gameSize AND targetPlayerIndex = :targetPlayerIndex")
    fun getCommanderDamageForPlayer(gameSize: Int, targetPlayerIndex: Int): Flow<List<CommanderDamage>>

    /**
     * Retrieves a reactive flow of all commander damage entries across all games.
     * This is primarily used by the repository to construct the global [com.example.mtglifetracker.viewmodel.GameState].
     *
     * @return A [Flow] emitting a list of all [CommanderDamage] objects in the database.
     */
    @Query("SELECT * FROM commander_damage")
    fun getAllDamage(): Flow<List<CommanderDamage>>

    /**
     * Retrieves the current damage value for a specific interaction.
     * This is a suspend function that performs a one-shot read of the database.
     *
     * @param gameSize The number of players in the game.
     * @param sourcePlayerIndex The index of the player dealing the damage.
     * @param targetPlayerIndex The index of the player receiving the damage.
     * @return The integer damage value, or null if no entry exists.
     */
    @Query("SELECT damage FROM commander_damage WHERE gameSize = :gameSize AND sourcePlayerIndex = :sourcePlayerIndex AND targetPlayerIndex = :targetPlayerIndex")
    suspend fun getDamageValue(gameSize: Int, sourcePlayerIndex: Int, targetPlayerIndex: Int): Int?

    /**
     * Atomically increments the commander damage for a specific player interaction by 1.
     *
     * @param gameSize The number of players in the game.
     * @param sourcePlayerIndex The index of the player dealing the damage.
     * @param targetPlayerIndex The index of the player receiving the damage.
     */
    @Query("UPDATE commander_damage SET damage = damage + 1 WHERE gameSize = :gameSize AND sourcePlayerIndex = :sourcePlayerIndex AND targetPlayerIndex = :targetPlayerIndex")
    suspend fun incrementCommanderDamage(gameSize: Int, sourcePlayerIndex: Int, targetPlayerIndex: Int)

    /**
     * Atomically decrements the commander damage for a specific player interaction by 1.
     * The damage value is capped at a minimum of 0 to prevent negative values.
     *
     * @param gameSize The number of players in the game.
     * @param sourcePlayerIndex The index of the player dealing the damage.
     * @param targetPlayerIndex The index of the player receiving the damage.
     */
    @Query("UPDATE commander_damage SET damage = MAX(0, damage - 1) WHERE gameSize = :gameSize AND sourcePlayerIndex = :sourcePlayerIndex AND targetPlayerIndex = :targetPlayerIndex")
    suspend fun decrementCommanderDamage(gameSize: Int, sourcePlayerIndex: Int, targetPlayerIndex: Int)

    /**
     * Counts the number of commander damage entries for a specific game size.
     * Useful for checking if the damage entries have been properly initialized.
     *
     * @param gameSize The game size to count entries for.
     * @return The total number of damage entries for that game size.
     */
    @Query("SELECT COUNT(*) FROM commander_damage WHERE gameSize = :gameSize")
    suspend fun getDamageEntryCountForGame(gameSize: Int): Int

    /**
     * Deletes all commander damage entries associated with a specific game size.
     * This is called when a game is reset.
     *
     * @param gameSize The game size for which to delete all commander damage entries.
     */
    @Query("DELETE FROM commander_damage WHERE gameSize = :gameSize")
    suspend fun deleteCommanderDamageForGame(gameSize: Int)

    /**
     * Deletes all entries from the `commander_damage` table.
     * This is a destructive operation used during a full application reset.
     */
    @Query("DELETE FROM commander_damage")
    suspend fun deleteAll()
}