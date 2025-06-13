package com.example.mtglifetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mtglifetracker.model.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile)

    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAll(): Flow<List<Profile>>

    // New method to find a profile by its nickname, ignoring case.
    @Query("SELECT * FROM profiles WHERE nickname = :nickname COLLATE NOCASE LIMIT 1")
    suspend fun getProfileByNickname(nickname: String): Profile?

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteById(profileId: Long)

    @Update
    suspend fun update(profile: Profile)

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getById(profileId: Long): Profile?
}