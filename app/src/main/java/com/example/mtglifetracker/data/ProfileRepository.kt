package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton repository for managing user-created profiles.
 *
 * This class abstracts the data source for [Profile] entities, providing a clean API
 * for ViewModels and other parts of the application to interact with profile data without
 * needing direct access to the [ProfileDao]. It handles all create, read, update, and
 * delete (CRUD) operations for profiles.
 *
 * @param profileDao The Data Access Object for the `profiles` table.
 */
@Singleton
class ProfileRepository @Inject constructor(private val profileDao: ProfileDao) {

    /**
     * A reactive flow that emits the full list of all saved [Profile] objects.
     * Observers will receive a new list whenever any profile is added, updated, or deleted.
     */
    val profiles = profileDao.getAll()

    /**
     * Adds a new profile to the database.
     *
     * @param profile The [Profile] object to be inserted.
     */
    suspend fun addProfile(profile: Profile) {
        Logger.i("ProfileRepository: Adding new profile with nickname '${profile.nickname}'.")
        profileDao.insert(profile)
    }

    /**
     * Checks if a profile with the given nickname already exists in the database.
     * The check is case-insensitive.
     *
     * @param nickname The nickname to check for existence.
     * @return `true` if a profile with the nickname exists, `false` otherwise.
     */
    suspend fun doesNicknameExist(nickname: String): Boolean {
        Logger.d("ProfileRepository: Checking for existence of nickname '$nickname'.")
        val exists = profileDao.getProfileByNickname(nickname) != null
        Logger.d("ProfileRepository: Nickname '$nickname' exists -> $exists.")
        return exists
    }

    /**
     * Deletes a profile from the database using its unique ID.
     *
     * @param profileId The ID of the profile to delete.
     */
    suspend fun deleteProfile(profileId: Long) {
        Logger.i("ProfileRepository: Deleting profile with ID $profileId.")
        profileDao.deleteById(profileId)
    }

    /**
     * Updates an existing profile in the database.
     * The profile is identified by the `id` field within the [Profile] object.
     *
     * @param profile The [Profile] object containing the updated data.
     */
    suspend fun updateProfile(profile: Profile) {
        Logger.i("ProfileRepository: Updating profile with ID ${profile.id}.")
        Logger.d("ProfileRepository: New data for profile ${profile.id}: Nickname='${profile.nickname}', Color='${profile.color}'.")
        profileDao.update(profile)
    }

    /**
     * Retrieves a single profile from the database by its unique ID.
     *
     * @param profileId The ID of the profile to fetch.
     * @return The found [Profile] object, or null if no profile with that ID exists.
     */
    suspend fun getProfile(profileId: Long): Profile? {
        Logger.d("ProfileRepository: Getting profile with ID $profileId.")
        return profileDao.getById(profileId)
    }
}