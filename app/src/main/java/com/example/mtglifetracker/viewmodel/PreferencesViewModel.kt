package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.PreferencesRepository
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel responsible for managing and exposing user preferences.
 *
 * This class acts as a bridge between the UI layer (specifically preference-related dialogs)
 * and the [PreferencesRepository]. It exposes the user's preferences as a reactive [StateFlow]
 * and provides methods to update them.
 *
 * @param repository The singleton [PreferencesRepository] instance provided by Hilt.
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val repository: PreferencesRepository
) : ViewModel() {

    /**
     * A [StateFlow] that emits the current user [Preferences].
     *
     * It is configured to:
     * - Map a null emission from the repository to a default [Preferences] object, ensuring the
     * flow always has a valid state.
     * - Be a "hot" flow using `stateIn`, meaning it keeps its latest value and can be shared
     * among multiple observers without re-executing the upstream flow.
     * - Stop the upstream flow after 5 seconds of no active observers (`SharingStarted.WhileSubscribed(5000)`),
     * which is an efficient way to manage resources.
     */
    val preferences: StateFlow<Preferences> = repository.preferences
        .map {
            // If the flow from the repository is null (e.g., first app launch), provide a default object.
            val currentPrefs = it ?: Preferences()
            Logger.d("PreferencesViewModel: Preferences flow emitted new value: deduceCommanderDamage=${currentPrefs.deduceCommanderDamage}")
            currentPrefs
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Preferences() // Provide an initial default value.
        )

    /**
     * Updates the user's preference for automatically deducting commander damage.
     *
     * @param deduce A boolean value indicating the new preference state.
     */
    fun setDeduceCommanderDamage(deduce: Boolean) {
        Logger.i("PreferencesViewModel: setDeduceCommanderDamage called.")
        Logger.d("PreferencesViewModel: Setting deduceCommanderDamage to '$deduce'.")
        viewModelScope.launch {
            // Create a new Preferences object with the updated value and save it.
            repository.savePreferences(preferences.value.copy(deduceCommanderDamage = deduce))
        }
    }
}