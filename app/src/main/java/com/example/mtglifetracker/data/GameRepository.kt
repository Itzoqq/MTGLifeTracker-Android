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
            players = gamePreferences.getPlayers(),
            playerDeltas = List(gamePreferences.getPlayerCount()) { 0 }
        )
    )
    val gameState = _gameState.asStateFlow()

    /**
     * Resets the game state for a new number of players.
     */
    fun changePlayerCount(newPlayerCount: Int) {
        val newPlayers = (1..newPlayerCount).map { Player(name = "Player $it") }
        _gameState.update {
            it.copy(
                playerCount = newPlayerCount,
                players = newPlayers,
                playerDeltas = List(newPlayerCount) { 0 },
                activeDeltaPlayers = emptySet()
            )
        }
        saveCurrentState()
    }

    /**
     * Increases a player's life and updates their transient delta counter.
     */
    fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    /**
     * Decreases a player's life and updates their transient delta counter.
     */
    fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    /**
     * Resets the delta counter for a single player back to 0 and marks the sequence as inactive.
     * This is called after the 3-second timeout.
     */
    fun resetDeltaForPlayer(playerIndex: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        val currentState = _gameState.value
        val updatedDeltas = currentState.playerDeltas.toMutableList()
        val updatedActivePlayers = currentState.activeDeltaPlayers.toMutableSet()

        if (!updatedActivePlayers.contains(playerIndex)) return // No change needed

        updatedDeltas[playerIndex] = 0
        updatedActivePlayers.remove(playerIndex)

        _gameState.update {
            it.copy(
                playerDeltas = updatedDeltas,
                activeDeltaPlayers = updatedActivePlayers
            )
        }
    }

    /**
     * A generic function to handle player state updates for life and deltas.
     * It now also manages the activeDeltaPlayers set.
     */
    private fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        if (playerIndex >= _gameState.value.players.size) return

        val currentState = _gameState.value
        val currentDelta = currentState.playerDeltas.getOrNull(playerIndex) ?: 0

        val updatedPlayers = currentState.players.mapIndexed { index, player ->
            if (index == playerIndex) player.copy(life = player.life + lifeChange) else player
        }
        val updatedDeltas = currentState.playerDeltas.toMutableList().apply {
            this[playerIndex] = currentDelta + lifeChange
        }
        // When a life change occurs, always add the player to the set of active deltas.
        val updatedActivePlayers = currentState.activeDeltaPlayers.toMutableSet().apply {
            add(playerIndex)
        }

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                playerDeltas = updatedDeltas,
                activeDeltaPlayers = updatedActivePlayers
            )
        }
        saveCurrentState()
    }

    /**
     * Persists the core game state to the data source.
     */
    private fun saveCurrentState() {
        val currentState = _gameState.value
        gamePreferences.savePlayerCount(currentState.playerCount)
        gamePreferences.savePlayers(currentState.players)
    }
}