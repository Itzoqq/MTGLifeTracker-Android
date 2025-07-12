package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Preferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(private val preferencesDao: PreferencesDao) {

    val preferences: Flow<Preferences?> = preferencesDao.getPreferences()

    suspend fun savePreferences(preferences: Preferences) {
        preferencesDao.savePreferences(preferences)
    }
}