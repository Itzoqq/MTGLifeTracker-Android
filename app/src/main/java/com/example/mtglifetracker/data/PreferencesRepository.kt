package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton repository responsible for managing user preferences.
 *
 * This class serves as an abstraction layer over the [PreferencesDao], providing a clean
 * API for the rest of the application to interact with user settings data. It exposes
 * a reactive flow of preferences and a method to save them.
 *
 * @param preferencesDao The Data Access Object for the `preferences` table.
 */
@Singleton
class PreferencesRepository @Inject constructor(private val preferencesDao: PreferencesDao) {

    /**
     * A reactive [Flow] that emits the current user [Preferences] object.
     * Observers of this flow will receive an update whenever the preferences are changed
     * in the database.
     */
    val preferences: Flow<Preferences?> = preferencesDao.getPreferences()

    /**
     * Saves the provided [Preferences] object to the database.
     *
     * This is a suspend function that will perform the database write operation on a
     * background thread.
     *
     * @param preferences The [Preferences] object to save.
     */
    suspend fun savePreferences(preferences: Preferences) {
        Logger.i("PreferencesRepository: Saving user preferences.")
        Logger.d("PreferencesRepository: Saving preferences with deduceCommanderDamage = ${preferences.deduceCommanderDamage}")
        preferencesDao.savePreferences(preferences)
    }
}