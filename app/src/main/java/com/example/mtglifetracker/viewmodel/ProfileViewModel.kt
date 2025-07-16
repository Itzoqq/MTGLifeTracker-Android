package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.ProfileRepository
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel responsible for managing all interactions with user profiles.
 *
 * This class provides the bridge between the UI (profile management dialogs) and the
 * [ProfileRepository]. It exposes a flow of all profiles and provides methods for
 * adding, updating, deleting, and validating profiles.
 *
 * @param repository The singleton [ProfileRepository] instance provided by Hilt.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    /**
     * A public, read-only flow that emits the current list of all [Profile] objects.
     * The UI observes this to display the list of saved profiles.
     */
    val profiles = repository.profiles

    /**
     * Adds a new profile to the database.
     * Performs a check to ensure the nickname is not blank before delegating to the repository.
     *
     * @param nickname The nickname for the new profile.
     * @param color The optional hex color string for the new profile.
     */
    fun addProfile(nickname: String, color: String?) {
        Logger.i("ProfileViewModel: addProfile called.")
        Logger.d("ProfileViewModel: Attempting to add profile with Nickname='$nickname', Color='$color'.")
        viewModelScope.launch {
            if (nickname.isNotBlank()) {
                repository.addProfile(Profile(nickname = nickname, color = color))
            } else {
                Logger.w("ProfileViewModel: addProfile called with a blank nickname. Aborting.")
            }
        }
    }

    /**
     * Checks if a given nickname already exists in the database.
     * This is a suspend function intended to be called from a coroutine in the UI layer
     * to perform validation before attempting to save a new profile.
     *
     * @param nickname The nickname to check.
     * @return `true` if the nickname already exists, `false` otherwise.
     */
    suspend fun doesNicknameExist(nickname: String): Boolean {
        Logger.d("ProfileViewModel: doesNicknameExist check for '$nickname'.")
        return repository.doesNicknameExist(nickname)
    }

    /**
     * Deletes a profile from the database using its unique ID.
     *
     * @param profileId The ID of the profile to be deleted.
     */
    fun deleteProfile(profileId: Long) {
        Logger.i("ProfileViewModel: deleteProfile called for ID $profileId.")
        viewModelScope.launch {
            repository.deleteProfile(profileId)
        }
    }

    /**
     * Updates an existing profile with new data.
     *
     * @param profile The [Profile] object containing the new data. The ID within the object
     * determines which profile gets updated.
     */
    fun updateProfile(profile: Profile) {
        Logger.i("ProfileViewModel: updateProfile called for ID ${profile.id}.")
        Logger.d("ProfileViewModel: Updating profile ${profile.id} with Nickname='${profile.nickname}', Color='${profile.color}'.")
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    /**
     * Retrieves a single profile by its unique ID.
     *
     * @param profileId The ID of the profile to fetch.
     * @return The found [Profile] object, or null if it doesn't exist.
     */
    suspend fun getProfile(profileId: Long): Profile? {
        Logger.d("ProfileViewModel: getProfile called for ID $profileId.")
        return repository.getProfile(profileId)
    }
}