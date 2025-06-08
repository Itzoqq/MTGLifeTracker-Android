package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList(),
    val playerDeltas: List<Int> = emptyList(),
    val activeDeltaPlayers: Set<Int> = emptySet()
)

@HiltViewModel
class GameViewModel @Inject constructor(private val repository: GameRepository) : ViewModel() {

    val gameState = repository.gameState

    private val timeoutJobs = mutableMapOf<Int, Job>()

    /**
     * MODIFIED: All calls to the repository are now wrapped in `viewModelScope.launch`.
     * This ensures that if the ViewModel is cleared, the coroutine is cancelled,
     * and the repository's suspend function will not complete.
     */
    fun changePlayerCount(newPlayerCount: Int) {
        viewModelScope.launch {
            repository.changePlayerCount(newPlayerCount)
        }
    }

    /**
     * MODIFIED: Wrapped in `viewModelScope.launch`.
     */
    fun resetCurrentGame() {
        viewModelScope.launch {
            repository.resetCurrentGame()
        }
    }

    /**
     * MODIFIED: Wrapped in `viewModelScope.launch`.
     */
    fun resetAllGames() {
        viewModelScope.launch {
            repository.resetAllGames()
        }
    }

    /**
     * MODIFIED: The call to the repository is now inside a viewModelScope.launch block.
     * The delta reset timer is also launched within this same scope.
     */
    fun increaseLife(playerIndex: Int) {
        cancelTimeout(playerIndex)
        viewModelScope.launch {
            repository.increaseLife(playerIndex)
            launchResetTimer(playerIndex)
        }
    }

    /**
     * MODIFIED: The call to the repository is now inside a viewModelScope.launch block.
     */
    fun decreaseLife(playerIndex: Int) {
        cancelTimeout(playerIndex)
        viewModelScope.launch {
            repository.decreaseLife(playerIndex)
            launchResetTimer(playerIndex)
        }
    }

    private fun cancelTimeout(playerIndex: Int) {
        timeoutJobs[playerIndex]?.cancel()
        timeoutJobs.remove(playerIndex)
    }

    /**
     * This now launches a coroutine within the existing viewModelScope,
     * which is fine as it's part of the same lifecycle.
     */
    private fun launchResetTimer(playerIndex: Int) {
        timeoutJobs[playerIndex] = viewModelScope.launch {
            delay(3000)
            repository.resetDeltaForPlayer(playerIndex)
        }
    }
}
