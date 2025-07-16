package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mtglifetracker.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `players` table.
 *
 * This interface defines all the necessary SQL operations for creating, reading,
 * updating, and deleting [Player] entities in the database. Each player is uniquely
 * identified by a composite primary key of their `gameSize` and `playerIndex`.
 *
 * Logging for these database calls is handled within the [GameRepository], which is
 * the sole caller of this DAO.
 */
@Dao
interface PlayerDao {

    /**
     * Inserts a list of players into the database.
     *
     * If a player with the same primary key (`gameSize` and `playerIndex`) already
     * exists, it will be replaced with the new data. This is useful for initializing
     * or resetting player states for a given game.
     *
     * @param players A list of [Player] objects to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<Player>)

    /**
     * Updates a single player's data in the database.
     *
     * The player is identified by its primary key. All of its fields will be updated
     * to match the provided [Player] object.
     *
     * @param player The [Player] object with updated information to save.
     */
    @Update
    suspend fun updatePlayer(player: Player)

    /**
     * Retrieves a reactive flow of all players for a specific game size.
     *
     * This method returns a [Flow] that will automatically emit a new list of players
     * whenever any player data for that `gameSize` changes. The results are ordered by
     * `playerIndex` to ensure a consistent player order.
     *
     * @param gameSize The number of players in the game (e.g., 2, 4).
     * @return A [Flow] emitting a list of [Player] objects for the specified game size.
     */
    @Query("SELECT * FROM players WHERE gameSize = :gameSize ORDER BY playerIndex ASC")
    fun getPlayers(gameSize: Int): Flow<List<Player>>

    /**
     * Deletes all players associated with a specific game size.
     * This is typically used when resetting the state for just one game layout.
     *
     * @param gameSize The game size for which to delete all associated player entries.
     */
    @Query("DELETE FROM players WHERE gameSize = :gameSize")
    suspend fun deletePlayersForGame(gameSize: Int)

    /**
     * Deletes all entries from the `players` table.
     * This is a destructive operation used during a full application reset.
     */
    @Query("DELETE FROM players")
    suspend fun deleteAll()

    /**
     * Retrieves a reactive flow of all players across all game sizes in the database.
     * This is primarily used by the repository to monitor changes for profile updates.
     *
     * @return A [Flow] emitting a list of all [Player] objects in the database.
     */
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>

    /**
     * Updates a list of players in the database.
     *
     * This is more efficient than calling `updatePlayer` in a loop, as it can often be
     * batched into a single database transaction. It's used for updating multiple
     * players at once, such as when their assigned profiles are edited or deleted.
     *
     * @param players A list of [Player] objects to update.
     */
    @Update
    suspend fun updatePlayers(players: List<Player>)
}