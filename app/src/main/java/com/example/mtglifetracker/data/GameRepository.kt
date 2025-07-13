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
            combine(
                settingsDao.getSettings().filterNotNull(),
                playerDao.getAllPlayers(),
                commanderDamageDao.getAllDamage()
            ) { settings, allPlayers, allDamage ->
                println("REPO_LOG: Combining game state: settings, allPlayers, allDamage.")
                val currentPlayers = allPlayers.filter { it.gameSize == settings.playerCount }
                println("REPO_LOG: Filtered players for game size ${settings.playerCount}. Found ${currentPlayers.size} players.")
                Triple(settings, currentPlayers, allDamage)
            }.collect { (settings, players, allDamage) ->
                println("REPO_LOG: Updating game state. Player count: ${settings.playerCount}, Players: ${players.size}")
                _gameState.update {
                    it.copy(
                        playerCount = settings.playerCount,
                        startingLife = settings.startingLife,
                        players = players,
                        allCommanderDamage = allDamage
                    )
                }
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
        println("REPO_LOG: Getting commander damage for player $targetPlayerIndex in game size $gameSize.")
        return commanderDamageDao.getCommanderDamageForPlayer(gameSize, targetPlayerIndex)
    }

    suspend fun incrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        val gameSize = _gameState.value.playerCount
        println("REPO_LOG: incrementCommanderDamage called. Source: $sourcePlayerIndex, Target: $targetPlayerIndex, Game Size: $gameSize")
        commanderDamageDao.incrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)
        val preferences = preferencesDao.getPreferences().first()
        println("REPO_LOG: Damage deduction preference is ${preferences?.deduceCommanderDamage}.")
        if (preferences?.deduceCommanderDamage == true) {
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
            val preferences = preferencesDao.getPreferences().first()
            println("REPO_LOG: Damage deduction preference is ${preferences?.deduceCommanderDamage}.")
            if (preferences?.deduceCommanderDamage == true) {
                println("REPO_LOG: Adding life back to player $targetPlayerIndex.")
                updatePlayerState(targetPlayerIndex, 1)
            }
        } else {
            println("REPO_LOG: Skipping decrement. Current damage is 0 or null.")
        }
    }

    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        println("REPO_LOG: updatePlayerState called for player $playerIndex with life change $lifeChange.")
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate == null) {
            println("REPO_LOG: ERROR: Player to update at index $playerIndex not found in current game state.")
            return
        }
        val updatedPlayer = playerToUpdate.copy(life = playerToUpdate.life + lifeChange)
        println("REPO_LOG: Updating player ${playerToUpdate.name}'s life from ${playerToUpdate.life} to ${updatedPlayer.life}.")
        playerDao.updatePlayer(updatedPlayer)
        println("REPO_LOG: Player update sent to DAO. State will update reactively.")
    }

    suspend fun increaseLife(playerIndex: Int) {
        println("REPO_LOG: increaseLife called for player $playerIndex.")
        updatePlayerState(playerIndex, 1)
    }

    suspend fun decreaseLife(playerIndex: Int) {
        println("REPO_LOG: decreaseLife called for player $playerIndex.")
        updatePlayerState(playerIndex, -1)
    }

    private suspend fun initializeDatabase() {
        println("REPO_LOG: Checking for initial settings...")
        val settings = settingsDao.getSettings().first()
        if (settings == null) {
            println("REPO_LOG: No settings found. Creating default settings and players.")
            val defaultSettings = GameSettings(playerCount = 2, startingLife = 40)
            settingsDao.saveSettings(defaultSettings)
            ensurePlayersExistForGameSize(defaultSettings.playerCount, defaultSettings.startingLife)
        } else {
            println("REPO_LOG: Settings found. Ensuring damage entries exist.")
            ensureDamageEntriesExist(settings.playerCount)
        }
    }

    private suspend fun ensureDamageEntriesExist(gameSize: Int) {
        println("REPO_LOG: Ensuring damage entries exist for game size $gameSize.")
        val expectedCount = gameSize * (gameSize - 1)
        val actualCount = commanderDamageDao.getDamageEntryCountForGame(gameSize)
        println("REPO_LOG: Expected damage entries: $expectedCount, Found: $actualCount.")

        if (actualCount < expectedCount) {
            println("REPO_LOG: Creating missing damage entries for game size $gameSize.")
            val newDamages = (0 until gameSize).flatMap { i ->
                (0 until gameSize).filter { j -> i != j }.map { j ->
                    CommanderDamage(gameSize, i, j, 0)
                }
            }
            commanderDamageDao.insertAll(newDamages)
            println("REPO_LOG: Inserted ${newDamages.size} new damage entries.")
        }
    }

    private suspend fun ensurePlayersExistForGameSize(gameSize: Int, startingLife: Int) {
        println("REPO_LOG: Ensuring players exist for game size $gameSize.")
        if (playerDao.getPlayers(gameSize).first().isEmpty()) {
            println("REPO_LOG: No players found for game size $gameSize. Creating new players.")
            val newPlayers = (0 until gameSize).map { index ->
                Player(
                    gameSize = gameSize,
                    playerIndex = index,
                    name = "Player ${index + 1}",
                    life = startingLife
                )
            }
            playerDao.insertAll(newPlayers)
            println("REPO_LOG: Inserted ${newPlayers.size} new players.")
        }
        ensureDamageEntriesExist(gameSize)
    }

    suspend fun changePlayerCount(newPlayerCount: Int) {
        println("REPO_LOG: changePlayerCount called with $newPlayerCount.")
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        ensurePlayersExistForGameSize(newPlayerCount, currentSettings.startingLife)
        println("REPO_LOG: Saving new player count setting.")
        settingsDao.saveSettings(currentSettings.copy(playerCount = newPlayerCount))
    }

    suspend fun changeStartingLife(newStartingLife: Int) {
        println("REPO_LOG: changeStartingLife called with $newStartingLife.")
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        settingsDao.saveSettings(currentSettings.copy(startingLife = newStartingLife))
        println("REPO_LOG: Starting life changed, resetting all games.")
        resetAllGames()
    }

    suspend fun resetCurrentGame() {
        println("REPO_LOG: resetCurrentGame called.")
        val currentSettings = settingsDao.getSettings().first()!!
        println("REPO_LOG: Deleting players and damage for current game size ${currentSettings.playerCount}.")
        playerDao.deletePlayersForGame(currentSettings.playerCount)
        commanderDamageDao.deleteCommanderDamageForGame(currentSettings.playerCount)
        ensurePlayersExistForGameSize(currentSettings.playerCount, currentSettings.startingLife)
        println("REPO_LOG: Current game reset complete.")
    }

    suspend fun resetAllGames() {
        println("REPO_LOG: resetAllGames called.")
        val currentSettings = settingsDao.getSettings().first()!!
        playerDao.deleteAll()
        commanderDamageDao.deleteAll()
        println("REPO_LOG: All players and damage deleted. Recreating for all game sizes.")
        (2..6).forEach { gameSize ->
            ensurePlayersExistForGameSize(gameSize, currentSettings.startingLife)
        }
        println("REPO_LOG: All games reset complete.")
    }

    suspend fun updatePlayerProfile(playerIndex: Int, profile: Profile) {
        println("REPO_LOG: updatePlayerProfile called for player $playerIndex with profile '${profile.nickname}'.")
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate != null) {
            val updatedPlayer = playerToUpdate.copy(
                name = profile.nickname,
                profileId = profile.id,
                color = profile.color
            )
            println("REPO_LOG: Updating player in DAO.")
            playerDao.updatePlayer(updatedPlayer)
        } else {
            println("REPO_LOG: ERROR: Player not found for profile update at index $playerIndex.")
        }
    }

    suspend fun unloadPlayerProfile(playerIndex: Int) {
        println("REPO_LOG: unloadPlayerProfile called for player $playerIndex.")
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate != null) {
            val defaultName = "Player ${playerIndex + 1}"
            val updatedPlayer = playerToUpdate.copy(name = defaultName, profileId = null, color = null)
            println("REPO_LOG: Unloading profile and updating player in DAO.")
            playerDao.updatePlayer(updatedPlayer)
        } else {
            println("REPO_LOG: ERROR: Player not found for profile unload at index $playerIndex.")
        }
    }
}