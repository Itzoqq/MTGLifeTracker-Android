package com.example.mtglifetracker.data

import com.example.mtglifetracker.SingletonIdlingResource
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.viewmodel.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val preferencesDao: PreferencesDao,
    externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        println("[DESIGN TRACE] GameRepository: Initializing...")

        externalScope.launch {
            println("[DESIGN TRACE] GameRepository init: Launching primary state collection coroutine.")
            initializeDatabase()
            println("[DESIGN TRACE] GameRepository init: Database initialization complete.")

            combine(
                settingsDao.getSettings().filterNotNull(),
                playerDao.getAllPlayers(),
                commanderDamageDao.getAllDamage()
            ) { settings, allPlayers, allDamage ->
                Triple(settings, allPlayers, allDamage)
            }.combine(preferencesDao.getPreferences().map { it ?: Preferences() }) { triple, preferences ->
                val (settings, allPlayers, allDamage) = triple
                println("[DESIGN TRACE] GameRepository state combine: Received updates. Settings players=${settings.playerCount}, All players in DB=${allPlayers.size}")

                val currentPlayers = allPlayers.filter { it.gameSize == settings.playerCount }
                println("[DESIGN TRACE] GameRepository state combine: Filtered to ${currentPlayers.size} players for game size ${settings.playerCount}.")

                GameState(
                    playerCount = settings.playerCount,
                    startingLife = settings.startingLife,
                    players = currentPlayers,
                    allCommanderDamage = allDamage,
                    deduceCommanderDamage = preferences.deduceCommanderDamage
                )
            }.collect { newGameState ->
                println("[DESIGN TRACE] GameRepository state collector: UPDATING a new game state. Players in new state: ${newGameState.players.size}")
                _gameState.value = newGameState
            }
        }

        externalScope.launch {
            println("[DESIGN TRACE] GameRepository init: Launching profile update collection coroutine.")
            combine(profileDao.getAll(), playerDao.getAllPlayers()) { allProfiles, allPlayersInDb ->
                println("[DESIGN TRACE] GameRepository profile combine: Received updates. Profiles: ${allProfiles.size}, Players in DB: ${allPlayersInDb.size}")
                val playersToUpdate = mutableListOf<Player>()
                allPlayersInDb.filter { it.profileId != null }.forEach { player ->
                    val matchingProfile = allProfiles.find { it.id == player.profileId }
                    if (matchingProfile != null) {
                        if (player.name != matchingProfile.nickname || player.color != matchingProfile.color) {
                            playersToUpdate.add(player.copy(name = matchingProfile.nickname, color = matchingProfile.color))
                        }
                    } else {
                        playersToUpdate.add(player.copy(name = "Player ${player.playerIndex + 1}", profileId = null, color = null))
                    }
                }
                playersToUpdate
            }.collect { playersToUpdate ->
                if (playersToUpdate.isNotEmpty()) {
                    println("[DESIGN TRACE] GameRepository profile collector: Found ${playersToUpdate.size} players to update based on profile changes.")
                    SingletonIdlingResource.increment()
                    try {
                        playerDao.updatePlayers(playersToUpdate)
                    } finally {
                        SingletonIdlingResource.decrement()
                    }
                } else {
                    println("[DESIGN TRACE] GameRepository profile collector: No player updates required.")
                }
            }
        }
    }

    fun getCommanderDamageForPlayer(targetPlayerIndex: Int): Flow<List<CommanderDamage>> {
        val gameSize = _gameState.value.playerCount
        return commanderDamageDao.getCommanderDamageForPlayer(gameSize, targetPlayerIndex)
    }

    suspend fun incrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        SingletonIdlingResource.increment()
        try {
            val gameSize = _gameState.value.playerCount
            commanderDamageDao.incrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)
            val deduceDamage = _gameState.value.deduceCommanderDamage
            if (deduceDamage) {
                updatePlayerState(targetPlayerIndex, -1)
            }
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun decrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        SingletonIdlingResource.increment()
        try {
            val gameSize = _gameState.value.playerCount
            val currentDamage = commanderDamageDao.getDamageValue(gameSize, sourcePlayerIndex, targetPlayerIndex)

            if (currentDamage != null && currentDamage > 0) {
                commanderDamageDao.decrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)
                val deduceDamage = _gameState.value.deduceCommanderDamage
                if (deduceDamage) {
                    updatePlayerState(targetPlayerIndex, 1)
                }
            }
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate == null) {
            println("[DESIGN TRACE ERROR] updatePlayerState: Could not find player at index $playerIndex.")
            return
        }
        val newLife = playerToUpdate.life + lifeChange
        val updatedPlayer = playerToUpdate.copy(life = newLife)
        playerDao.updatePlayer(updatedPlayer)
    }

    suspend fun increaseLife(playerIndex: Int) {
        SingletonIdlingResource.increment()
        try {
            updatePlayerState(playerIndex, 1)
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun decreaseLife(playerIndex: Int) {
        SingletonIdlingResource.increment()
        try {
            updatePlayerState(playerIndex, -1)
        } finally {
            SingletonIdlingResource.decrement()
        }
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
            val newDamages = (0 until gameSize).flatMap { i ->
                (0 until gameSize).filter { j -> i != j }.map { j ->
                    CommanderDamage(gameSize, i, j, 0)
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
        SingletonIdlingResource.increment()
        try {
            val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
            ensurePlayersExistForGameSize(newPlayerCount, currentSettings.startingLife)
            settingsDao.saveSettings(currentSettings.copy(playerCount = newPlayerCount))
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun changeStartingLife(newStartingLife: Int) {
        SingletonIdlingResource.increment()
        try {
            val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
            settingsDao.saveSettings(currentSettings.copy(startingLife = newStartingLife))
            resetAllGames()
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun resetCurrentGame() {
        SingletonIdlingResource.increment()
        try {
            val currentSettings = settingsDao.getSettings().first()!!
            playerDao.deletePlayersForGame(currentSettings.playerCount)
            commanderDamageDao.deleteCommanderDamageForGame(currentSettings.playerCount)
            ensurePlayersExistForGameSize(currentSettings.playerCount, currentSettings.startingLife)
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun resetAllGames() {
        SingletonIdlingResource.increment()
        try {
            val currentSettings = settingsDao.getSettings().first()!!
            playerDao.deleteAll()
            commanderDamageDao.deleteAll()
            (2..6).forEach { gameSize ->
                ensurePlayersExistForGameSize(gameSize, currentSettings.startingLife)
            }
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun updatePlayerProfile(playerIndex: Int, profile: Profile) {
        SingletonIdlingResource.increment()
        try {
            println("[DESIGN TRACE] updatePlayerProfile: Attempting to find player at index $playerIndex. Current state has ${_gameState.value.players.size} players.")
            val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
            if (playerToUpdate != null) {
                val updatedPlayer = playerToUpdate.copy(
                    name = profile.nickname,
                    profileId = profile.id,
                    color = profile.color
                )
                playerDao.updatePlayer(updatedPlayer)
            } else {
                println("[DESIGN TRACE ERROR] updatePlayerProfile: Could not find player at index $playerIndex to assign profile.")
            }
        } finally {
            SingletonIdlingResource.decrement()
        }
    }

    suspend fun unloadPlayerProfile(playerIndex: Int) {
        SingletonIdlingResource.increment()
        try {
            println("[DESIGN TRACE] unloadPlayerProfile: Attempting to find player at index $playerIndex. Current state has ${_gameState.value.players.size} players.")
            val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
            if (playerToUpdate != null) {
                val defaultName = "Player ${playerIndex + 1}"
                val updatedPlayer = playerToUpdate.copy(name = defaultName, profileId = null, color = null)
                playerDao.updatePlayer(updatedPlayer)
            } else {
                println("[DESIGN TRACE ERROR] unloadPlayerProfile: Could not find player at index $playerIndex to unload profile.")
            }
        } finally {
            SingletonIdlingResource.decrement()
        }
    }
}