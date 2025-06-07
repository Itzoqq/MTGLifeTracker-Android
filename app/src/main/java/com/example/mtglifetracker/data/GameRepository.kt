package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages all game data, acting as the single source of truth for the application's state.
 * It uses a data source (like GamePreferences) to load initial state and persist changes.
 *
 * @param gamePreferences The data source for saving and loading game state.
 */
class GameRepository(private val gamePreferences: GamePreferences) {

    private val _gameState = MutableStateFlow(
        GameState(
            playerCount = gamePreferences.getPlayerCount(),
            players = gamePreferences.getPlayers()
        )
    )
    val gameState = _gameState.asStateFlow()

    /**
     * Resets the game state for a new number of players, creating new Player objects.
     */
    fun changePlayerCount(newPlayerCount: Int) {
        val newPlayers = (1..newPlayerCount).map { Player(name = "Player $it") }
        _gameState.update {
            it.copy(
                playerCount = newPlayerCount,
                players = newPlayers
            )
        }
        saveCurrentState()
    }

    /**
     * Increases a player's life by creating a new list with an updated, immutable Player object.
     */
    fun increaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        val updatedPlayers = _gameState.value.players.mapIndexed { index, player ->
            if (index == playerIndex) {
                player.copy(life = player.life + 1)
            } else {
                player
            }
        }
        _gameState.update { it.copy(players = updatedPlayers) }
        saveCurrentState()
    }

    /**
     * Decreases a player's life by creating a new list with an updated, immutable Player object.
     */
    fun decreaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        val updatedPlayers = _gameState.value.players.mapIndexed { index, player ->
            if (index == playerIndex) {
                player.copy(life = player.life - 1)
            } else {
                player
            }
        }
        _gameState.update { it.copy(players = updatedPlayers) }
        saveCurrentState()
    }

    /**
     * Persists the current game state to the data source.
     */
    private fun saveCurrentState() {
        val currentState = _gameState.value
        gamePreferences.savePlayerCount(currentState.playerCount)
        gamePreferences.savePlayers(currentState.players)
    }
}