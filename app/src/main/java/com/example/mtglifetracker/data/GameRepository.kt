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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val settingsDao: GameSettingsDao,
    // This scope is now only used for the long-running collector in the init block.
    // This is the correct use for an application-level scope.
    externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        // This coroutine should live for as long as the application is alive,
        // so using the externalScope here is the correct approach. It ensures
        // the app is always listening for database changes.
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

    /**
     * MODIFIED: This is now a suspend function.
     * The responsibility of choosing a CoroutineScope is moved to the caller (the ViewModel).
     * This makes the repository more testable and respects the caller's lifecycle.
     */
    suspend fun changePlayerCount(newPlayerCount: Int) {
        ensurePlayersExistForGameSize(newPlayerCount)
        settingsDao.saveSettings(GameSettings(playerCount = newPlayerCount))
    }

    /**
     * MODIFIED: Converted to a suspend function.
     * It no longer launches its own coroutine.
     */
    suspend fun resetCurrentGame() {
        val currentGameSize = _gameState.value.playerCount
        playerDao.deletePlayersForGame(currentGameSize)
        ensurePlayersExistForGameSize(currentGameSize)
    }

    /**
     * MODIFIED: Converted to a suspend function.
     */
    suspend fun resetAllGames() {
        playerDao.deleteAll()
        val currentGameSize = _gameState.value.playerCount
        ensurePlayersExistForGameSize(currentGameSize)
    }

    /**
     * MODIFIED: This is now a suspend function that handles the full update logic.
     */
    suspend fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    /**
     * MODIFIED: This is now a suspend function that handles the full update logic.
     */
    suspend fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    /**
     * MODIFIED: This is now a suspend function.
     * The database write operation (`playerDao.updatePlayer`) is a suspend call
     * that will run within the scope provided by the ViewModel.
     * The `externalScope.launch` has been removed.
     */
    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val currentState = _gameState.value
        if (playerIndex >= currentState.players.size || playerIndex >= currentState.playerDeltas.size) {
            return
        }

        val playerToUpdate = currentState.players[playerIndex]
        val updatedPlayer = playerToUpdate.copy(life = playerToUpdate.life + lifeChange)

        // The database update is now called directly as a suspend function.
        // It will run on the dispatcher provided by the ViewModel's scope (typically Dispatchers.Main)
        // but Room ensures the actual DB work is done on a background thread.
        playerDao.updatePlayer(updatedPlayer)

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
