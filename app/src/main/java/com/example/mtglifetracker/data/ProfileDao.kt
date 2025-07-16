package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mtglifetracker.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `profiles` table.
 *
 * This interface defines all the necessary SQL operations for creating, reading,
 * updating, and deleting [Profile] entities. Profiles allow users to save
 * named configurations (nickname and color) that can be quickly assigned to players.
 *
 * Logging for these database calls is handled within the [GameRepository] and
 * [ProfileRepository], which are the callers of this DAO.
 */
@Dao
interface ProfileDao {

    /**
     * Inserts a new profile into the database.
     *
     * If a profile with the same ID already exists, it will be replaced. This is
     * generally not expected for new insertions since the ID is auto-generated,
     * but the REPLACE strategy provides robustness.
     *
     * @param profile The [Profile] object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile)

    /**
     * Retrieves a reactive flow of all saved profiles, ordered by their ID.
     *
     * @return A [Flow] that emits an updated list of all [Profile] objects
     * whenever the `profiles` table is modified.
     */
    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAll(): Flow<List<Profile>>

    /**
     * Finds a single profile by its nickname.
     *
     * The search is case-insensitive (`COLLATE NOCASE`) to prevent duplicate nicknames
     * that only differ by capitalization (e.g., "PlayerOne" and "playerone").
     *
     * @param nickname The nickname to search for.
     * @return The matching [Profile] object, or null if no profile with that nickname exists.
     */
    @Query("SELECT * FROM profiles WHERE nickname = :nickname COLLATE NOCASE LIMIT 1")
    suspend fun getProfileByNickname(nickname: String): Profile?

    /**
     * Deletes a profile from the database based on its unique ID.
     *
     * @param profileId The auto-generated ID of the profile to be deleted.
     */
    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteById(profileId: Long)

    /**
     * Updates an existing profile in the database.
     *
     * The profile to be updated is identified by its primary key (`id`).
     *
     * @param profile The [Profile] object containing the updated information.
     */
    @Update
    suspend fun update(profile: Profile)

    /**
     * Retrieves a single profile by its unique ID.
     *
     * @param profileId The ID of the profile to retrieve.
     * @return The matching [Profile] object, or null if not found.
     */
    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getById(profileId: Long): Profile?
}