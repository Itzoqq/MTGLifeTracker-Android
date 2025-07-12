package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.PreferencesRepository
import com.example.mtglifetracker.model.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val repository: PreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<Preferences> = repository.preferences
        .map { it ?: Preferences() } // If the flow emits null, provide a default object
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Preferences()
        )

    fun setDeduceCommanderDamage(deduce: Boolean) {
        viewModelScope.launch {
            repository.savePreferences(preferences.value.copy(deduceCommanderDamage = deduce))
        }
    }
}