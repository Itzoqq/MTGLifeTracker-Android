package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.ProfileRepository
import com.example.mtglifetracker.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    val profiles = repository.profiles

    fun addProfile(nickname: String, color: String?) {
        viewModelScope.launch {
            if (nickname.isNotBlank()) {
                repository.addProfile(Profile(nickname = nickname, color = color))
            }
        }
    }

    // New suspend function for the UI to call for validation.
    suspend fun doesNicknameExist(nickname: String): Boolean {
        return repository.doesNicknameExist(nickname)
    }
}