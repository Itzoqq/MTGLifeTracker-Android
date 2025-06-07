package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.Player

// Data class remains the same
data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList()
)

/**
 * ViewModel that connects the UI to the Repository.
 * It doesn't contain any logic itself, it just forwards commands.
 */
class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Expose the game state directly from the repository
    val gameState = repository.gameState

    fun changePlayerCount(newPlayerCount: Int) {
        repository.changePlayerCount(newPlayerCount)
    }

    fun increaseLife(playerIndex: Int) {
        repository.increaseLife(playerIndex)
    }

    fun decreaseLife(playerIndex: Int) {
        repository.decreaseLife(playerIndex)
    }
}