package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameRepository(
    private val playerDao: PlayerDao,
    private val settingsDao: GameSettingsDao,
    private val externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        // This collector updates the players list in the state whenever it changes in the DB
        externalScope.launch {
            playerDao.getAllPlayers().collect { players ->
                _gameState.update { it.copy(players = players) }
            }
        }

        // This collector updates the player count and resets deltas if the count changes
        externalScope.launch {
            settingsDao.getSettings().collect { settings ->
                val currentSettings = settings ?: GameSettings()
                _gameState.update { currentState ->
                    // THIS IS THE FIX:
                    // We now *always* reset the transient state (deltas) when settings change.
                    // This ensures the lists are always the correct size and avoids the crash.
                    currentState.copy(
                        playerCount = currentSettings.playerCount,
                        playerDeltas = List(currentSettings.playerCount) { 0 },
                        activeDeltaPlayers = emptySet()
                    )
                }
            }
        }
        // This ensures the database is seeded with default values on the very first launch
        initializeDatabaseIfNeeded()
    }

    private fun initializeDatabaseIfNeeded() {
        externalScope.launch {
            if (settingsDao.getSettings().first() == null) {
                val defaultSettings = GameSettings(playerCount = 2)
                settingsDao.saveSettings(defaultSettings)
                val newPlayers = (1..defaultSettings.playerCount).map { Player(name = "Player $it") }
                playerDao.insertAll(newPlayers)
            }
        }
    }

    fun changePlayerCount(newPlayerCount: Int) {
        externalScope.launch {
            settingsDao.saveSettings(GameSettings(playerCount = newPlayerCount))
            playerDao.deleteAll()
            val newPlayers = (1..newPlayerCount).map { Player(name = "Player $it") }
            playerDao.insertAll(newPlayers)
        }
    }

    fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    private fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        if (playerIndex >= _gameState.value.players.size || playerIndex >= _gameState.value.playerDeltas.size) {
            return
        }

        val currentState = _gameState.value
        val playerToUpdate = currentState.players[playerIndex]
        val updatedPlayer = playerToUpdate.copy(life = playerToUpdate.life + lifeChange)

        externalScope.launch(Dispatchers.IO) {
            playerDao.updatePlayer(updatedPlayer)
        }

        val currentDelta = currentState.playerDeltas[playerIndex]
        val updatedDeltas = currentState.playerDeltas.toMutableList().apply {
            this[playerIndex] = currentDelta + lifeChange
        }
        val updatedActivePlayers = currentState.activeDeltaPlayers.toMutableSet().apply {
            add(playerIndex)
        }
        _gameState.update {
            it.copy(
                playerDeltas = updatedDeltas,
                activeDeltaPlayers = updatedActivePlayers
            )
        }
    }

    fun resetDeltaForPlayer(playerIndex: Int) {
        if (playerIndex >= _gameState.value.playerDeltas.size) return

        val currentState = _gameState.value
        val updatedDeltas = currentState.playerDeltas.toMutableList()
        val updatedActivePlayers = currentState.activeDeltaPlayers.toMutableSet()

        if (!updatedActivePlayers.contains(playerIndex)) return

        updatedDeltas[playerIndex] = 0
        updatedActivePlayers.remove(playerIndex)

        _gameState.update {
            it.copy(
                playerDeltas = updatedDeltas,
                activeDeltaPlayers = updatedActivePlayers
            )
        }
    }
}