package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile
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
                        val settings = settingsDao.getSettings().first()
                        val newPlayerCount = settings?.playerCount ?: currentState.playerCount
                        // Simply update the player list and count from the source of truth (DB)
                        currentState.copy(
                            playerCount = newPlayerCount,
                            players = players
                        )
                    }
                }
        }
    }

    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val currentState = _gameState.value
        val playerToUpdate = currentState.players.getOrNull(playerIndex) ?: return

        val updatedPlayer = playerToUpdate.copy(life = playerToUpdate.life + lifeChange)

        // Update the state flow immediately for a responsive UI
        val updatedPlayers = currentState.players.toMutableList().apply {
            this[playerIndex] = updatedPlayer
        }
        _gameState.update { it.copy(players = updatedPlayers) }

        // Persist the change to the database, which will trigger the collect block above
        playerDao.updatePlayer(updatedPlayer)
    }

    suspend fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    suspend fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    // Unchanged methods
    private suspend fun initializeDatabase() {
        if (settingsDao.getSettings().first() == null) {
            val defaultSettings = GameSettings(playerCount = 2, startingLife = 40)
            settingsDao.saveSettings(defaultSettings)
            ensurePlayersExistForGameSize(defaultSettings.playerCount, defaultSettings.startingLife)
        }
    }

    private suspend fun ensurePlayersExistForGameSize(gameSize: Int, startingLife: Int) {
        if (playerDao.getPlayers(gameSize).first().isEmpty()) {
            val newPlayers = (0 until gameSize).map { index ->
                Player(
                    gameSize = gameSize,
                    playerIndex = index,
                    name = "Player ${index + 1}",
                    life = startingLife
                )
            }
            playerDao.insertAll(newPlayers)
        }
    }
    suspend fun changePlayerCount(newPlayerCount: Int) {
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        ensurePlayersExistForGameSize(newPlayerCount, currentSettings.startingLife)
        settingsDao.saveSettings(currentSettings.copy(playerCount = newPlayerCount))
    }


    suspend fun changeStartingLife(newStartingLife: Int) {
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        settingsDao.saveSettings(currentSettings.copy(startingLife = newStartingLife))
        resetAllGames() // Reset games to apply new life total
    }

    suspend fun resetCurrentGame() {
        val currentSettings = settingsDao.getSettings().first()!!
        playerDao.deletePlayersForGame(currentSettings.playerCount)
        ensurePlayersExistForGameSize(currentSettings.playerCount, currentSettings.startingLife)
    }

    suspend fun resetAllGames() {
        val currentSettings = settingsDao.getSettings().first()!!
        playerDao.deleteAll()
        (2..6).forEach { gameSize ->
            ensurePlayersExistForGameSize(gameSize, currentSettings.startingLife)
        }
    }

    suspend fun updatePlayerProfile(playerIndex: Int, profile: Profile) {
        val currentState = _gameState.value
        val playerToUpdate = currentState.players.getOrNull(playerIndex)

        if (playerToUpdate != null) {
            val updatedPlayer = playerToUpdate.copy(
                name = profile.nickname,
                profileId = profile.id,
                color = profile.color
            )
            playerDao.updatePlayer(updatedPlayer)
        }
    }

    suspend fun unloadPlayerProfile(playerIndex: Int) {
        val currentState = _gameState.value
        val playerToUpdate = currentState.players.getOrNull(playerIndex)

        if (playerToUpdate != null) {
            val defaultName = "Player ${playerIndex + 1}"
            val updatedPlayer = playerToUpdate.copy(name = defaultName, profileId = null, color = null)
            playerDao.updatePlayer(updatedPlayer)
        }
    }
}