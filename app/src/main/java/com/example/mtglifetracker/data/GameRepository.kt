package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Suppress("RedundantConstructorKeyword")
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository constructor(
    private val playerDao: PlayerDao,
    private val settingsDao: GameSettingsDao,
    externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        externalScope.launch {
            initializeDatabase()
            settingsDao.getSettings()
                .flatMapLatest { settings ->
                    val playerCount = settings!!.playerCount
                    playerDao.getPlayers(playerCount)
                }
                .collect { players ->
                    _gameState.update { currentState ->
                        val newPlayerCount = players.firstOrNull()?.gameSize ?: currentState.playerCount
                        val playerCountChanged = currentState.playerCount != newPlayerCount
                        val deltasNeedInit = currentState.playerDeltas.size != players.size

                        if (playerCountChanged || deltasNeedInit) {
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

    private suspend fun initializeDatabase() {
        if (settingsDao.getSettings().first() == null) {
            val defaultSettings = GameSettings(playerCount = 2)
            settingsDao.saveSettings(defaultSettings)
            ensurePlayersExistForGameSize(defaultSettings.playerCount)
        }
    }

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

    suspend fun changePlayerCount(newPlayerCount: Int) {
        ensurePlayersExistForGameSize(newPlayerCount)
        settingsDao.saveSettings(GameSettings(playerCount = newPlayerCount))
    }

    suspend fun resetCurrentGame() {
        val currentGameSize = _gameState.value.playerCount
        playerDao.deletePlayersForGame(currentGameSize)
        ensurePlayersExistForGameSize(currentGameSize)
    }

    suspend fun resetAllGames() {
        playerDao.deleteAll()
        val currentGameSize = _gameState.value.playerCount
        ensurePlayersExistForGameSize(currentGameSize)
    }

    suspend fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    suspend fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        var playerToUpdate: Player? = null

        _gameState.update { currentState ->
            // Get the player from the most current state
            val player = currentState.players.getOrNull(playerIndex)
            if (player == null) {
                return@update currentState // Abort if player not found
            }

            // Calculate the new player state
            val updatedPlayer = player.copy(life = player.life + lifeChange)
            playerToUpdate = updatedPlayer // Store for database update

            val updatedPlayers = currentState.players.toMutableList().apply {
                this[playerIndex] = updatedPlayer // Use the non-null updatedPlayer
            }

            // Calculate the new delta state
            val currentDelta = currentState.playerDeltas.getOrElse(playerIndex) { 0 }
            val updatedDeltas = currentState.playerDeltas.toMutableList().apply {
                this[playerIndex] = currentDelta + lifeChange
            }
            val updatedActivePlayers = currentState.activeDeltaPlayers + playerIndex

            // Return the new state with all changes applied at once
            currentState.copy(
                players = updatedPlayers,
                playerDeltas = updatedDeltas,
                activeDeltaPlayers = updatedActivePlayers
            )
        }

        // Now that the state is updated, persist the change to the database
        playerToUpdate?.let {
            playerDao.updatePlayer(it)
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