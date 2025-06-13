package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Profile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val profileDao: ProfileDao) {

    val profiles = profileDao.getAll()

    suspend fun addProfile(profile: Profile) {
        profileDao.insert(profile)
    }

    // New method to check for nickname existence using the DAO.
    suspend fun doesNicknameExist(nickname: String): Boolean {
        return profileDao.getProfileByNickname(nickname) != null
    }

    suspend fun deleteProfile(profileId: Long) {
        profileDao.deleteById(profileId)
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.update(profile)
    }

    suspend fun getProfile(profileId: Long): Profile? {
        return profileDao.getById(profileId)
    }
}