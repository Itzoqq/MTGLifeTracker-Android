package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository to manage all game data. It is the single source of truth for the app's state.
 * It uses GamePreferences to persist data.
 */
class GameRepository(private val gamePreferences: GamePreferences) {

    // Load initial state from preferences when the repository is created
    private val _gameState = MutableStateFlow(
        GameState(
            playerCount = gamePreferences.getPlayerCount(),
            players = gamePreferences.getPlayers()
        )
    )
    val gameState = _gameState.asStateFlow()

    fun changePlayerCount(newPlayerCount: Int) {
        val newPlayers = (1..newPlayerCount).map { Player(name = "Player $it") }
        _gameState.update {
            it.copy(
                playerCount = newPlayerCount,
                players = newPlayers
            )
        }
        // Save the new state
        saveCurrentState()
    }

    fun increaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return
        val updatedPlayers = _gameState.value.players.toMutableList()
        updatedPlayers[playerIndex].increaseLife()
        _gameState.update { it.copy(players = updatedPlayers) }
        // Save the new state
        saveCurrentState()
    }

    fun decreaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return
        val updatedPlayers = _gameState.value.players.toMutableList()
        updatedPlayers[playerIndex].decreaseLife()
        _gameState.update { it.copy(players = updatedPlayers) }
        // Save the new state
        saveCurrentState()
    }

    /**
     * Saves the current game state to SharedPreferences.
     */
    private fun saveCurrentState() {
        val currentState = _gameState.value
        gamePreferences.savePlayerCount(currentState.playerCount)
        gamePreferences.savePlayers(currentState.players)
    }
}