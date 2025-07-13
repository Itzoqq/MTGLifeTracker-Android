package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The GameState now contains the starting life total.
 */
data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList(),
    val startingLife: Int = 40,
    val allCommanderDamage: List<CommanderDamage> = emptyList()
)

@HiltViewModel
class GameViewModel @Inject constructor(private val repository: GameRepository) : ViewModel() {

    val gameState = repository.gameState

    fun changePlayerCount(newPlayerCount: Int) {
        viewModelScope.launch {
            repository.changePlayerCount(newPlayerCount)
        }
    }

    fun changeStartingLife(newStartingLife: Int) {
        viewModelScope.launch {
            repository.changeStartingLife(newStartingLife)
        }
    }

    fun resetCurrentGame() {
        viewModelScope.launch {
            repository.resetCurrentGame()
        }
    }

    fun resetAllGames() {
        viewModelScope.launch {
            repository.resetAllGames()
        }
    }

    fun increaseLife(playerIndex: Int) {
        viewModelScope.launch {
            repository.increaseLife(playerIndex)
        }
    }

    fun decreaseLife(playerIndex: Int) {
        viewModelScope.launch {
            repository.decreaseLife(playerIndex)
        }
    }

    fun setPlayerProfile(playerIndex: Int, profile: Profile) {
        viewModelScope.launch {
            repository.updatePlayerProfile(playerIndex, profile)
        }
    }

    fun unloadProfile(playerIndex: Int) {
        viewModelScope.launch {
            repository.unloadPlayerProfile(playerIndex)
        }
    }

    fun getCommanderDamageForPlayer(targetPlayerIndex: Int): Flow<List<CommanderDamage>> {
        return repository.getCommanderDamageForPlayer(targetPlayerIndex)
    }

    fun incrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        viewModelScope.launch {
            repository.incrementCommanderDamage(sourcePlayerIndex, targetPlayerIndex)
        }
    }

    fun decrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        viewModelScope.launch {
            repository.decrementCommanderDamage(sourcePlayerIndex, targetPlayerIndex)
        }
    }
}