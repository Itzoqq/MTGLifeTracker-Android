package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Suppress("RedundantConstructorKeyword")
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository constructor(
    private val playerDao: PlayerDao,
    private val settingsDao: GameSettingsDao,
    private val profileDao: ProfileDao,
    private val commanderDamageDao: CommanderDamageDao,
    externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        externalScope.launch {
            initializeDatabase()
            settingsDao.getSettings()
                .filterNotNull()
                .flatMapLatest { settings ->
                    playerDao.getPlayers(settings.playerCount)
                }
                .collect { players ->
                    _gameState.update { currentState ->
                        val settings = settingsDao.getSettings().first()
                        val newPlayerCount = settings?.playerCount ?: currentState.playerCount
                        currentState.copy(
                            playerCount = newPlayerCount,
                            players = players
                        )
                    }
                }
        }

        externalScope.launch {
            combine(profileDao.getAll(), playerDao.getAllPlayers()) { allProfiles, allPlayersInDb ->
                val playersToUpdate = mutableListOf<Player>()

                allPlayersInDb.filter { it.profileId != null }.forEach { player ->
                    val matchingProfile = allProfiles.find { it.id == player.profileId }
                    if (matchingProfile != null) {
                        if (player.name != matchingProfile.nickname || player.color != matchingProfile.color) {
                            playersToUpdate.add(
                                player.copy(
                                    name = matchingProfile.nickname,
                                    color = matchingProfile.color
                                )
                            )
                        }
                    } else {
                        playersToUpdate.add(
                            player.copy(
                                name = "Player ${player.playerIndex + 1}",
                                profileId = null,
                                color = null
                            )
                        )
                    }
                }
                playersToUpdate
            }.collect { playersToUpdate ->
                if (playersToUpdate.isNotEmpty()) {
                    playerDao.updatePlayers(playersToUpdate)
                }
            }
        }
    }

    fun getCommanderDamageForPlayer(targetPlayerIndex: Int): Flow<List<CommanderDamage>> {
        val gameSize = _gameState.value.playerCount
        return commanderDamageDao.getCommanderDamageForPlayer(gameSize, targetPlayerIndex)
    }

    suspend fun incrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        val gameSize = _gameState.value.playerCount
        commanderDamageDao.incrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)
    }

    suspend fun decrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        val gameSize = _gameState.value.playerCount
        commanderDamageDao.decrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)
    }

    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val currentState = _gameState.value
        val playerToUpdate = currentState.players.getOrNull(playerIndex) ?: return

        val updatedPlayer = playerToUpdate.copy(life = playerToUpdate.life + lifeChange)

        val updatedPlayers = currentState.players.toMutableList().apply {
            this[playerIndex] = updatedPlayer
        }
        _gameState.update { it.copy(players = updatedPlayers) }

        playerDao.updatePlayer(updatedPlayer)
    }

    suspend fun increaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, 1)
    }

    suspend fun decreaseLife(playerIndex: Int) {
        updatePlayerState(playerIndex, -1)
    }

    private suspend fun initializeDatabase() {
        val settings = settingsDao.getSettings().first()
        if (settings == null) {
            val defaultSettings = GameSettings(playerCount = 2, startingLife = 40)
            settingsDao.saveSettings(defaultSettings)
            ensurePlayersExistForGameSize(defaultSettings.playerCount, defaultSettings.startingLife)
        } else {
            ensureDamageEntriesExist(settings.playerCount)
        }
    }

    private suspend fun ensureDamageEntriesExist(gameSize: Int) {
        val expectedCount = gameSize * (gameSize - 1)
        val actualCount = commanderDamageDao.getDamageEntryCountForGame(gameSize)

        if (actualCount < expectedCount) {
            val newDamages = mutableListOf<CommanderDamage>()
            for (i in 0 until gameSize) {
                for (j in 0 until gameSize) {
                    if (i != j) {
                        newDamages.add(CommanderDamage(gameSize, i, j, 0))
                    }
                }
            }
            commanderDamageDao.insertAll(newDamages)
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
        ensureDamageEntriesExist(gameSize)
    }

    suspend fun changePlayerCount(newPlayerCount: Int) {
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        ensurePlayersExistForGameSize(newPlayerCount, currentSettings.startingLife)
        settingsDao.saveSettings(currentSettings.copy(playerCount = newPlayerCount))
    }

    suspend fun changeStartingLife(newStartingLife: Int) {
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        settingsDao.saveSettings(currentSettings.copy(startingLife = newStartingLife))
        resetAllGames()
    }

    suspend fun resetCurrentGame() {
        val currentSettings = settingsDao.getSettings().first()!!
        playerDao.deletePlayersForGame(currentSettings.playerCount)
        commanderDamageDao.deleteCommanderDamageForGame(currentSettings.playerCount)
        ensurePlayersExistForGameSize(currentSettings.playerCount, currentSettings.startingLife)
    }

    suspend fun resetAllGames() {
        val currentSettings = settingsDao.getSettings().first()!!
        playerDao.deleteAll()
        commanderDamageDao.deleteAll()
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