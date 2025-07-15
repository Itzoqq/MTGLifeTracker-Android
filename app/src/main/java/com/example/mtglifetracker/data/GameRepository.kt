package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.*
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
    private val preferencesDao: PreferencesDao,
    externalScope: CoroutineScope
) {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        println("REPO_LOG: GameRepository initializing...")
        externalScope.launch {
            println("REPO_LOG: Initializing database...")
            initializeDatabase()
            println("REPO_LOG: Setting up combined flows for game state.")

            // THE FIX: Nest a second combine for preferences instead of using a Quadruple
            combine(
                settingsDao.getSettings().filterNotNull(),
                playerDao.getAllPlayers(),
                commanderDamageDao.getAllDamage()
            ) { settings, allPlayers, allDamage ->
                Triple(settings, allPlayers, allDamage)
            }.combine(preferencesDao.getPreferences().map { it ?: Preferences() }) { triple, preferences ->
                // This lambda receives the result of the first combine (the Triple)
                // and the result of the preferences flow.
                val (settings, allPlayers, allDamage) = triple
                val currentPlayers = allPlayers.filter { it.gameSize == settings.playerCount }

                // Create the final GameState object here
                GameState(
                    playerCount = settings.playerCount,
                    startingLife = settings.startingLife,
                    players = currentPlayers,
                    allCommanderDamage = allDamage,
                    deduceCommanderDamage = preferences.deduceCommanderDamage
                )
            }.collect { newGameState ->
                // The collected item is now the fully formed GameState
                println("REPO_LOG: Updating game state. Player count: ${newGameState.playerCount}, Players: ${newGameState.players.size}, DeduceDmg: ${newGameState.deduceCommanderDamage}")
                _gameState.value = newGameState
            }
        }

        externalScope.launch {
            println("REPO_LOG: Setting up combined flows for profile updates.")
            combine(profileDao.getAll(), playerDao.getAllPlayers()) { allProfiles, allPlayersInDb ->
                println("REPO_LOG: Combining profiles and players for updates. Profiles: ${allProfiles.size}, Players in DB: ${allPlayersInDb.size}")
                val playersToUpdate = mutableListOf<Player>()
                allPlayersInDb.filter { it.profileId != null }.forEach { player ->
                    val matchingProfile = allProfiles.find { it.id == player.profileId }
                    if (matchingProfile != null) {
                        if (player.name != matchingProfile.nickname || player.color != matchingProfile.color) {
                            println("REPO_LOG: Profile change detected for player ${player.playerIndex}. Updating name/color.")
                            playersToUpdate.add(player.copy(name = matchingProfile.nickname, color = matchingProfile.color))
                        }
                    } else {
                        println("REPO_LOG: Profile for player ${player.playerIndex} deleted. Reverting to default.")
                        playersToUpdate.add(player.copy(name = "Player ${player.playerIndex + 1}", profileId = null, color = null))
                    }
                }
                playersToUpdate
            }.collect { playersToUpdate ->
                if (playersToUpdate.isNotEmpty()) {
                    println("REPO_LOG: Found ${playersToUpdate.size} players to update based on profile changes. Updating now.")
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
        println("REPO_LOG: incrementCommanderDamage called. Source: $sourcePlayerIndex, Target: $targetPlayerIndex, Game Size: $gameSize")
        commanderDamageDao.incrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)

        // Read the preference directly from the reactive gameState
        val deduceDamage = _gameState.value.deduceCommanderDamage
        println("REPO_LOG: Damage deduction preference is $deduceDamage.")
        if (deduceDamage) {
            println("REPO_LOG: Deducting life from player $targetPlayerIndex.")
            updatePlayerState(targetPlayerIndex, -1)
        }
    }

    suspend fun decrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        val gameSize = _gameState.value.playerCount
        val currentDamage = commanderDamageDao.getDamageValue(gameSize, sourcePlayerIndex, targetPlayerIndex)
        println("REPO_LOG: decrementCommanderDamage called. Source: $sourcePlayerIndex, Target: $targetPlayerIndex, Current Damage: $currentDamage")

        if (currentDamage != null && currentDamage > 0) {
            commanderDamageDao.decrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)

            // Read the preference directly from the reactive gameState
            val deduceDamage = _gameState.value.deduceCommanderDamage
            println("REPO_LOG: Damage deduction preference is $deduceDamage.")
            if (deduceDamage) {
                println("REPO_LOG: Adding life back to player $targetPlayerIndex.")
                updatePlayerState(targetPlayerIndex, 1)
            }
        } else {
            println("REPO_LOG: Skipping decrement. Current damage is 0 or null.")
        }
    }

    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate == null) {
            return
        }
        val updatedPlayer = playerToUpdate.copy(life = playerToUpdate.life + lifeChange)
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
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
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
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate != null) {
            val defaultName = "Player ${playerIndex + 1}"
            val updatedPlayer = playerToUpdate.copy(name = defaultName, profileId = null, color = null)
            playerDao.updatePlayer(updatedPlayer)
        }
    }
}