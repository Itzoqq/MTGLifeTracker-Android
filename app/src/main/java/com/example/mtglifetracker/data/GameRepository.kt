package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameRepository(private val gamePreferences: GamePreferences) {

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
        saveCurrentState()
    }

    fun increaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        // Create a new list with a new, updated Player object
        val updatedPlayers = _gameState.value.players.mapIndexed { index, player ->
            if (index == playerIndex) {
                player.copy(life = player.life + 1) // Create a copy with the new value
            } else {
                player
            }
        }
        _gameState.update { it.copy(players = updatedPlayers) }
        saveCurrentState()
    }

    fun decreaseLife(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        // Create a new list with a new, updated Player object
        val updatedPlayers = _gameState.value.players.mapIndexed { index, player ->
            if (index == playerIndex) {
                player.copy(life = player.life - 1) // Create a copy with the new value
            } else {
                player
            }
        }
        _gameState.update { it.copy(players = updatedPlayers) }
        saveCurrentState()
    }

    private fun saveCurrentState() {
        val currentState = _gameState.value
        gamePreferences.savePlayerCount(currentState.playerCount)
        gamePreferences.savePlayers(currentState.players)
    }
}