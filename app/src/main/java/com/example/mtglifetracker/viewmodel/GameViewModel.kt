package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mtglifetracker.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Data class to hold the entire state of the game UI
data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList()
)

class GameViewModel : ViewModel() {

    // Private MutableStateFlow that holds the current game state
    private val _gameState = MutableStateFlow(GameState())
    // Public immutable StateFlow that the UI can observe
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    init {
        // Initialize the game with the default state (2 players)
        changePlayerCount(2)
    }

    /**
     * Resets the game state for a new number of players.
     */
    fun changePlayerCount(newPlayerCount: Int) {
        val newPlayers = (1..newPlayerCount).map { Player(name = "Player $it") }
        _gameState.update {
            it.copy(
                playerCount = newPlayerCount,
                players = newPlayers
            )
        }
    }

    /**
     * Increases the life total for a specific player.
     */
    fun increaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        val updatedPlayers = _gameState.value.players.toMutableList()
        updatedPlayers[playerIndex].increaseLife()

        _gameState.update { it.copy(players = updatedPlayers) }
    }

    /**
     * Decreases the life total for a specific player.
     */
    fun decreaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        val updatedPlayers = _gameState.value.players.toMutableList()
        updatedPlayers[playerIndex].decreaseLife()

        _gameState.update { it.copy(players = updatedPlayers) }
    }
}