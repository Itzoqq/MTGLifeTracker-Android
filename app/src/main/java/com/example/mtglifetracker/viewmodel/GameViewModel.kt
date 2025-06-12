package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap // Import ConcurrentHashMap
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

    // Use ConcurrentHashMap for explicit thread safety.
    private val timeoutJobs = ConcurrentHashMap<Int, Job>()

    fun changePlayerCount(newPlayerCount: Int) {
        viewModelScope.launch {
            repository.changePlayerCount(newPlayerCount)
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
        // Delegate to the new centralized function
        updateLife(playerIndex) {
            repository.increaseLife(playerIndex)
        }
    }

    fun decreaseLife(playerIndex: Int) {
        // Delegate to the new centralized function
        updateLife(playerIndex) {
            repository.decreaseLife(playerIndex)
        }
    }

    /**
     * Centralized function to handle life updates and restart the delta timer.
     * This avoids repeating logic and makes the process clearer.
     *
     * @param playerIndex The index of the player being updated.
     * @param lifeUpdateAction The suspend function to execute (increase or decrease life).
     */
    private fun updateLife(playerIndex: Int, lifeUpdateAction: suspend () -> Unit) {
        // Cancel any existing timer job for this player. This is a thread-safe operation.
        timeoutJobs[playerIndex]?.cancel()

        // Launch a new coroutine to handle the entire flow.
        // We store the new Job to make it cancellable.
        timeoutJobs[playerIndex] = viewModelScope.launch {
            // 1. Perform the actual life update in the repository.
            lifeUpdateAction()

            // 2. Wait for 3 seconds.
            delay(3000)

            // 3. Reset the delta for the player.
            repository.resetDeltaForPlayer(playerIndex)
        }
    }
}