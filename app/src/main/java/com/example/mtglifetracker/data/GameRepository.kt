package com.example.mtglifetracker.data

import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger
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

/**
 * A singleton repository that serves as the single source of truth for all game-related data.
 *
 * This class abstracts the data sources (DAOs for players, settings, profiles, etc.) from the
 * rest of the application, particularly the ViewModels. It is responsible for fetching, combining,
 * and processing data streams into a coherent [GameState] that the UI can observe. It also
 * exposes methods to modify the game state, which it translates into the appropriate
 * database operations.
 *
 * @param playerDao The DAO for player data.
 * @param settingsDao The DAO for game settings.
 * @param profileDao The DAO for player profiles.
 * @param commanderDamageDao The DAO for commander damage.
 * @param preferencesDao The DAO for user preferences.
 * @param externalScope A [CoroutineScope] provided by Hilt for managing the lifecycle of
 * repository-level coroutines. This ensures that data flows remain active for the
 * application's lifetime.
 */
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

    // A private MutableStateFlow to hold the current game state. This is the internal state.
    private val _gameState = MutableStateFlow(GameState())
    // A public, read-only StateFlow that external classes can observe for UI updates.
    val gameState = _gameState.asStateFlow()

    init {
        Logger.i("GameRepository: Initializing...")

        // Main coroutine to combine all necessary data flows into a single GameState.
        externalScope.launch {
            Logger.d("GameRepository init: Launching primary state collection coroutine.")
            // First, ensure the database is initialized with default settings if it's empty.
            initializeDatabase()
            Logger.d("GameRepository init: Database initialization complete.")

            // This nested combine block listens to changes from multiple DAOs.
            // It first combines settings, players, and damage into a Triple.
            combine(
                settingsDao.getSettings().filterNotNull(),
                playerDao.getAllPlayers(),
                commanderDamageDao.getAllDamage()
            ) { settings, allPlayers, allDamage ->
                Triple(settings, allPlayers, allDamage)
            }.combine(preferencesDao.getPreferences().map { it ?: Preferences() }) { triple, preferences ->
                // This second combine adds the preferences flow.
                // When any of the source flows emit a new value, this block re-executes.
                val (settings, allPlayers, allDamage) = triple
                Logger.d("GameRepository state combine: Received updates. Settings players=${settings.playerCount}, All players in DB=${allPlayers.size}, All damage entries=${allDamage.size}, Prefs=${preferences.deduceCommanderDamage}")

                // Filter the list of all players to get only those for the current game size.
                val currentPlayers = allPlayers.filter { it.gameSize == settings.playerCount }
                Logger.d("GameRepository state combine: Filtered to ${currentPlayers.size} players for game size ${settings.playerCount}.")

                // Construct the new, complete GameState object.
                GameState(
                    playerCount = settings.playerCount,
                    startingLife = settings.startingLife,
                    players = currentPlayers,
                    allCommanderDamage = allDamage,
                    deduceCommanderDamage = preferences.deduceCommanderDamage
                )
            }.collect { newGameState ->
                // Emit the newly constructed GameState to all observers.
                Logger.i("GameRepository state collector: Updating game state. Player count: ${newGameState.playerCount}, Players: ${newGameState.players.size}, DeduceDmg: ${newGameState.deduceCommanderDamage}")
                _gameState.value = newGameState
            }
        }

        // A separate coroutine to handle automatic updates to players when their assigned profile changes.
        externalScope.launch {
            Logger.d("GameRepository init: Launching profile update collection coroutine.")
            combine(profileDao.getAll(), playerDao.getAllPlayers()) { allProfiles, allPlayersInDb ->
                Logger.d("GameRepository profile combine: Received updates. Profiles: ${allProfiles.size}, Players in DB: ${allPlayersInDb.size}")
                val playersToUpdate = mutableListOf<Player>()
                // Find all players who have a profile assigned.
                allPlayersInDb.filter { it.profileId != null }.forEach { player ->
                    val matchingProfile = allProfiles.find { it.id == player.profileId }
                    if (matchingProfile != null) {
                        // If the profile exists, check if the player's name or color is out of sync.
                        if (player.name != matchingProfile.nickname || player.color != matchingProfile.color) {
                            Logger.i("GameRepository profile combine: Profile change detected for player ${player.playerIndex}. Updating name/color.")
                            playersToUpdate.add(player.copy(name = matchingProfile.nickname, color = matchingProfile.color))
                        }
                    } else {
                        // If the profile no longer exists (i.e., it was deleted), revert the player to defaults.
                        Logger.i("GameRepository profile combine: Profile for player ${player.playerIndex} deleted. Reverting to default.")
                        playersToUpdate.add(player.copy(name = "Player ${player.playerIndex + 1}", profileId = null, color = null))
                    }
                }
                playersToUpdate
            }.collect { playersToUpdate ->
                // If any players need updating, perform the database operation.
                if (playersToUpdate.isNotEmpty()) {
                    Logger.i("GameRepository profile collector: Found ${playersToUpdate.size} players to update based on profile changes. Updating now.")
                    playerDao.updatePlayers(playersToUpdate)
                } else {
                    Logger.d("GameRepository profile collector: No player updates required based on profile changes.")
                }
            }
        }
    }

    /**
     * Retrieves a reactive flow of commander damage dealt *to* a specific player.
     *
     * @param targetPlayerIndex The index of the player receiving the damage.
     * @return A [Flow] that emits the list of [CommanderDamage] dealt to that player.
     */
    fun getCommanderDamageForPlayer(targetPlayerIndex: Int): Flow<List<CommanderDamage>> {
        val gameSize = _gameState.value.playerCount
        Logger.d("getCommanderDamageForPlayer: Fetching damage for player $targetPlayerIndex in game size $gameSize.")
        return commanderDamageDao.getCommanderDamageForPlayer(gameSize, targetPlayerIndex)
    }

    /**
     * Increments commander damage and optionally deducts life from the target player.
     *
     * @param sourcePlayerIndex The index of the player dealing the damage.
     * @param targetPlayerIndex The index of the player receiving the damage.
     */
    suspend fun incrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        val gameSize = _gameState.value.playerCount
        Logger.d("incrementCommanderDamage: Args(source=$sourcePlayerIndex, target=$targetPlayerIndex, gameSize=$gameSize)")
        commanderDamageDao.incrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)

        // Read the deduction preference directly from the current reactive game state.
        val deduceDamage = _gameState.value.deduceCommanderDamage
        Logger.d("incrementCommanderDamage: Damage deduction preference is $deduceDamage.")
        if (deduceDamage) {
            Logger.d("incrementCommanderDamage: Deducting 1 life from player $targetPlayerIndex.")
            updatePlayerState(targetPlayerIndex, -1)
        }
    }

    /**
     * Decrements commander damage and optionally adds life back to the target player.
     * Will not decrement below zero.
     *
     * @param sourcePlayerIndex The index of the player who dealt the damage.
     * @param targetPlayerIndex The index of the player who received the damage.
     */
    suspend fun decrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        val gameSize = _gameState.value.playerCount
        val currentDamage = commanderDamageDao.getDamageValue(gameSize, sourcePlayerIndex, targetPlayerIndex)
        Logger.d("decrementCommanderDamage: Args(source=$sourcePlayerIndex, target=$targetPlayerIndex, gameSize=$gameSize), currentDamage=$currentDamage")

        if (currentDamage != null && currentDamage > 0) {
            commanderDamageDao.decrementCommanderDamage(gameSize, sourcePlayerIndex, targetPlayerIndex)
            val deduceDamage = _gameState.value.deduceCommanderDamage
            Logger.d("decrementCommanderDamage: Damage deduction preference is $deduceDamage.")
            if (deduceDamage) {
                Logger.d("decrementCommanderDamage: Adding 1 life back to player $targetPlayerIndex.")
                updatePlayerState(targetPlayerIndex, 1)
            }
        } else {
            Logger.w("decrementCommanderDamage: Skipping decrement. Current damage is 0 or null.")
        }
    }

    /**
     * A centralized method to update a player's state, primarily for life changes.
     *
     * @param playerIndex The index of the player to update.
     * @param lifeChange The amount to change the life by (can be positive or negative).
     */
    private suspend fun updatePlayerState(playerIndex: Int, lifeChange: Int) {
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate == null) {
            Logger.e(null, "updatePlayerState: Could not find player at index $playerIndex. Aborting update.")
            return
        }
        val newLife = playerToUpdate.life + lifeChange
        Logger.d("updatePlayerState: Updating player $playerIndex life from ${playerToUpdate.life} to $newLife.")
        val updatedPlayer = playerToUpdate.copy(life = newLife)
        playerDao.updatePlayer(updatedPlayer)
    }

    /**
     * Increases a player's life total by 1.
     */
    suspend fun increaseLife(playerIndex: Int) {
        Logger.d("increaseLife: Called for player $playerIndex.")
        updatePlayerState(playerIndex, 1)
    }

    /**
     * Decreases a player's life total by 1.
     */
    suspend fun decreaseLife(playerIndex: Int) {
        Logger.d("decreaseLife: Called for player $playerIndex.")
        updatePlayerState(playerIndex, -1)
    }

    /**
     * Ensures the database is in a valid state on first launch. If no settings exist,
     * it creates default settings and the corresponding player/damage entries.
     */
    private suspend fun initializeDatabase() {
        val settings = settingsDao.getSettings().first()
        if (settings == null) {
            Logger.i("initializeDatabase: No settings found. Initializing with default values.")
            val defaultSettings = GameSettings(playerCount = 2, startingLife = 40)
            settingsDao.saveSettings(defaultSettings)
            ensurePlayersExistForGameSize(defaultSettings.playerCount, defaultSettings.startingLife)
        } else {
            Logger.d("initializeDatabase: Settings found. Ensuring damage entries exist for player count ${settings.playerCount}.")
            ensureDamageEntriesExist(settings.playerCount)
        }
    }

    /**
     * Checks if the correct number of commander damage entries exist for a given game size
     * and creates them if they are missing.
     *
     * @param gameSize The number of players in the game.
     */
    private suspend fun ensureDamageEntriesExist(gameSize: Int) {
        val expectedCount = gameSize * (gameSize - 1)
        val actualCount = commanderDamageDao.getDamageEntryCountForGame(gameSize)
        Logger.d("ensureDamageEntriesExist: For game size $gameSize, expected $expectedCount entries, found $actualCount.")

        if (actualCount < expectedCount) {
            Logger.i("ensureDamageEntriesExist: Missing damage entries for game size $gameSize. Creating them now.")
            val newDamages = (0 until gameSize).flatMap { i ->
                (0 until gameSize).filter { j -> i != j }.map { j ->
                    CommanderDamage(gameSize, i, j, 0)
                }
            }
            commanderDamageDao.insertAll(newDamages)
        }
    }

    /**
     * Checks if player entries exist for a given game size and creates them if they are missing.
     * Also ensures corresponding commander damage entries are created.
     *
     * @param gameSize The number of players in the game.
     * @param startingLife The life total to initialize new players with.
     */
    private suspend fun ensurePlayersExistForGameSize(gameSize: Int, startingLife: Int) {
        if (playerDao.getPlayers(gameSize).first().isEmpty()) {
            Logger.i("ensurePlayersExistForGameSize: No players found for game size $gameSize. Creating them now with starting life $startingLife.")
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
        // Always ensure damage entries are also present
        ensureDamageEntriesExist(gameSize)
    }

    /**
     * Changes the active player count. This involves updating the game settings and ensuring
     * the necessary player entries exist in the database for the new game size.
     *
     * @param newPlayerCount The desired number of players.
     */
    suspend fun changePlayerCount(newPlayerCount: Int) {
        Logger.i("changePlayerCount: Changing player count to $newPlayerCount.")
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        ensurePlayersExistForGameSize(newPlayerCount, currentSettings.startingLife)
        settingsDao.saveSettings(currentSettings.copy(playerCount = newPlayerCount))
    }

    /**
     * Changes the starting life total for all games. This is a destructive action that
     * resets all player data across all game sizes to reflect the new starting life.
     *
     * @param newStartingLife The new starting life total.
     */
    suspend fun changeStartingLife(newStartingLife: Int) {
        Logger.i("changeStartingLife: Changing starting life to $newStartingLife. This will reset all games.")
        val currentSettings = settingsDao.getSettings().first() ?: GameSettings()
        settingsDao.saveSettings(currentSettings.copy(startingLife = newStartingLife))
        resetAllGames()
    }

    /**
     * Resets the game state for the currently active player count only.
     * This involves deleting and re-creating the players and commander damage entries
     * for the current game size.
     */
    suspend fun resetCurrentGame() {
        val currentSettings = settingsDao.getSettings().first()!!
        Logger.i("resetCurrentGame: Resetting game for ${currentSettings.playerCount} players.")
        playerDao.deletePlayersForGame(currentSettings.playerCount)
        commanderDamageDao.deleteCommanderDamageForGame(currentSettings.playerCount)
        ensurePlayersExistForGameSize(currentSettings.playerCount, currentSettings.startingLife)
    }

    /**
     * Resets all game states across all supported player counts (2-6).
     * This is a destructive operation that clears all player and commander damage data
     * and re-initializes it based on the current starting life setting.
     */
    suspend fun resetAllGames() {
        val currentSettings = settingsDao.getSettings().first()!!
        Logger.i("resetAllGames: Resetting all games to starting life ${currentSettings.startingLife}.")
        Logger.d("resetAllGames: Deleting all player and commander damage data.")
        playerDao.deleteAll()
        commanderDamageDao.deleteAll()
        // Re-initialize players for all supported game sizes
        (2..6).forEach { gameSize ->
            Logger.d("resetAllGames: Ensuring players exist for game size $gameSize.")
            ensurePlayersExistForGameSize(gameSize, currentSettings.startingLife)
        }
    }

    /**
     * Assigns a [Profile] to a player. This updates the player's name and color to match
     * the profile and stores the association via the profile ID.
     *
     * @param playerIndex The index of the player to update.
     * @param profile The profile to assign to the player.
     */
    suspend fun updatePlayerProfile(playerIndex: Int, profile: Profile) {
        Logger.i("updatePlayerProfile: Assigning profile '${profile.nickname}' to player $playerIndex.")
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate != null) {
            val updatedPlayer = playerToUpdate.copy(
                name = profile.nickname,
                profileId = profile.id,
                color = profile.color
            )
            playerDao.updatePlayer(updatedPlayer)
        } else {
            Logger.e(null, "updatePlayerProfile: Could not find player at index $playerIndex to assign profile.")
        }
    }

    /**
     * Removes a profile assignment from a player, reverting their name and color
     * to the default state.
     *
     * @param playerIndex The index of the player whose profile should be unloaded.
     */
    suspend fun unloadPlayerProfile(playerIndex: Int) {
        Logger.i("unloadPlayerProfile: Unloading profile from player $playerIndex.")
        val playerToUpdate = _gameState.value.players.getOrNull(playerIndex)
        if (playerToUpdate != null) {
            val defaultName = "Player ${playerIndex + 1}"
            val updatedPlayer = playerToUpdate.copy(name = defaultName, profileId = null, color = null)
            playerDao.updatePlayer(updatedPlayer)
        } else {
            Logger.e(null, "unloadPlayerProfile: Could not find player at index $playerIndex to unload profile.")
        }
    }
}