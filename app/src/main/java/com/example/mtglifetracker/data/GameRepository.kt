package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository(
    private val playerDao: PlayerDao,
    private val settingsDao: GameSettingsDao,
    private val externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        externalScope.launch {
            // First, ensure the database is initialized before we start collecting flows.
            // This prevents race conditions on the first launch.
            initializeDatabase()

            // Main reactive stream. It listens for changes in player count
            // and switches to the corresponding player data stream from the database.
            settingsDao.getSettings()
                .flatMapLatest { settings ->
                    // After initialization, settings should never be null.
                    val playerCount = settings!!.playerCount
                    playerDao.getPlayers(playerCount)
                }
                .collect { players ->
                    // This collector receives player lists for the active game size.
                    _gameState.update { currentState ->
                        val newPlayerCount = players.firstOrNull()?.gameSize ?: currentState.playerCount

                        // If the player count has changed, reset the transient UI state (deltas).
                        val needsReset = currentState.playerCount != newPlayerCount

                        if (needsReset) {
                            currentState.copy(
                                playerCount = newPlayerCount,
                                players = players,
                                playerDeltas = List(newPlayerCount) { 0 },
                                activeDeltaPlayers = emptySet()
                            )
                        } else {
                            currentState.copy(players = players)
                        }
                    }
                }
        }
    }

    /**
     * A suspending function that checks for initial data and creates it if missing.
     * This runs to completion before any flows are collected.
     */
    private suspend fun initializeDatabase() {
        if (settingsDao.getSettings().first() == null) {
            val defaultSettings = GameSettings(playerCount = 2)
            settingsDao.saveSettings(defaultSettings)
            ensurePlayersExistForGameSize(defaultSettings.playerCount)
        }
    }

    /**
     * Checks if players for a given game size exist in the database.
     * If not, it creates and inserts a default set of players.
     */
    private suspend fun ensurePlayersExistForGameSize(gameSize: Int) {
        if (playerDao.getPlayers(gameSize).first().isEmpty()) {
            val newPlayers = (0 until gameSize).map { index ->
                Player(
                    gameSize = gameSize,
                    playerIndex = index,
                    name = "Player ${index + 1}"
                )
            }
            playerDao.insertAll(newPlayers)
        }
    }

    /**
     * Changes the active player count. It also ensures that player entries exist
     * for the new count before the settings are updated, which will trigger the flow.
     */
    fun changePlayerCount(newPlayerCount: Int) {
        externalScope.launch {
            ensurePlayersExistForGameSize(newPlayerCount)
            settingsDao.saveSettings(GameSettings(playerCount = newPlayerCount))
        }
    }

    fun resetCurrentGame() {
        externalScope.launch {
            val currentGameSize = _gameState.value.playerCount
            playerDao.deletePlayersForGame(currentGameSize)
            ensurePlayersExistForGameSize(currentGameSize)
        }
    }

    fun resetAllGames() {
        externalScope.launch {
            playerDao.deleteAll()
            val currentGameSize = _gameState.value.playerCount
            ensurePlayersExistForGameSize(currentGameSize)
        }
    }

    fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    private fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val currentState = _gameState.value
        if (playerIndex >= currentState.players.size || playerIndex >= currentState.playerDeltas.size) {
            return
        }

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