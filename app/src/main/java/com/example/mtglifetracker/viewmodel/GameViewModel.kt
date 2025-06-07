package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.Player

/**
 * Represents the complete state of the game UI at any given time.
 */
data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList()
)

/**
 * The ViewModel for the main game screen. It connects the UI (Activity) to the data layer (Repository).
 * It holds no state or logic itself, acting only as a pass-through.
 *
 * @param repository The repository that manages the game data.
 */
class GameViewModel(private val repository: GameRepository) : ViewModel() {

    /** Exposes the game state flow from the repository directly to the UI. */
    val gameState = repository.gameState

    /** Forwards the request to change the player count to the repository. */
    fun changePlayerCount(newPlayerCount: Int) {
        repository.changePlayerCount(newPlayerCount)
    }

    /** Forwards the request to increase life to the repository. */
    fun increaseLife(playerIndex: Int) {
        repository.increaseLife(playerIndex)
    }

    /** Forwards the request to decrease life to the repository. */
    fun decreaseLife(playerIndex: Int) {
        repository.decreaseLife(playerIndex)
    }
}