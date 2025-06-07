package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Represents the complete state of the game UI at any given time.
 * @property playerCount The number of players in the current game.
 * @property players The list of [Player] objects with their current state.
 * @property playerDeltas A list tracking the transient life changes for each player for the UI.
 * @property activeDeltaPlayers A set containing the indices of players with an active delta sequence.
 */
data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList(),
    val playerDeltas: List<Int> = emptyList(),
    val activeDeltaPlayers: Set<Int> = emptySet()
)

/**
 * The ViewModel for the main game screen. It connects the UI (Activity) to the data layer (Repository)
 * and manages UI-specific logic like the delta counter reset timer.
 *
 * @param repository The repository that manages the game data.
 */
class GameViewModel(private val repository: GameRepository) : ViewModel() {

    /** Exposes the game state flow from the repository directly to the UI. */
    val gameState = repository.gameState

    // A map to hold the cancellable timeout coroutine job for each player's delta counter.
    private val timeoutJobs = mutableMapOf<Int, Job>()

    /** Forwards the request to change the player count to the repository. */
    fun changePlayerCount(newPlayerCount: Int) {
        repository.changePlayerCount(newPlayerCount)
    }

    /**
     * Forwards the request to reset the current game state to the repository.
     */
    fun resetCurrentGame() {
        repository.resetCurrentGame()
    }

    /**
     * Forwards the request to reset all game states to the repository.
     */
    fun resetAllGames() {
        repository.resetAllGames()
    }

    /**
     * Increases a player's life and manages the reset timer for the delta display.
     * @param playerIndex The index of the player to update.
     */
    fun increaseLife(playerIndex: Int) {
        cancelTimeout(playerIndex)
        repository.increaseLife(playerIndex)
        launchResetTimer(playerIndex)
    }

    /**
     * Decreases a player's life and manages the reset timer for the delta display.
     * @param playerIndex The index of the player to update.
     */
    fun decreaseLife(playerIndex: Int) {
        cancelTimeout(playerIndex)
        repository.decreaseLife(playerIndex)
        launchResetTimer(playerIndex)
    }

    /**
     * Cancels any existing timeout job for a specific player.
     */
    private fun cancelTimeout(playerIndex: Int) {
        timeoutJobs[playerIndex]?.cancel()
        timeoutJobs.remove(playerIndex)
    }

    /**
     * Launches a new 3-second coroutine. After the delay, it resets the delta
     * counter for the specified player.
     */
    private fun launchResetTimer(playerIndex: Int) {
        timeoutJobs[playerIndex] = viewModelScope.launch {
            delay(3000)
            repository.resetDeltaForPlayer(playerIndex)
        }
    }
}