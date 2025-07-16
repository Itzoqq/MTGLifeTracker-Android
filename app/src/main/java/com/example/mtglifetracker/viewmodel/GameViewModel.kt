package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the complete, observable state of a game, including player data,
 * game settings, and commander damage information.
 *
 * @property playerCount The number of players in the current game setup.
 * @property players The list of [Player] objects for the current game.
 * @property startingLife The life total players start with.
 * @property allCommanderDamage A list of all [CommanderDamage] entries across all games.
 * @property deduceCommanderDamage A boolean indicating if commander damage should auto-deduct life.
 */
data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList(),
    val startingLife: Int = 40,
    val allCommanderDamage: List<CommanderDamage> = emptyList(),
    val deduceCommanderDamage: Boolean = true
)

/**
 * The ViewModel responsible for managing and exposing the [GameState] to the UI.
 *
 * This class acts as a bridge between the UI layer (e.g., [com.example.mtglifetracker.MainActivity]) and the
 * data layer ([GameRepository]). It exposes the game state as a reactive flow and provides
 * methods that the UI can call to trigger game logic, such as changing player counts,
 * updating life totals, or resetting the game. All calls to modify the state are delegated
 * to the repository.
 *
 * @param repository The singleton [GameRepository] instance provided by Hilt, which serves
 * as the single source of truth for all game data.
 */
@HiltViewModel
class GameViewModel @Inject constructor(private val repository: GameRepository) : ViewModel() {

    /**
     * A public, read-only [kotlinx.coroutines.flow.StateFlow] that emits the latest [GameState].
     * The UI layer observes this flow to reactively update itself whenever the game state changes.
     */
    val gameState = repository.gameState

    /**
     * Initiates a change in the number of players for the active game.
     * This action is delegated to the repository.
     *
     * @param newPlayerCount The desired number of players.
     */
    fun changePlayerCount(newPlayerCount: Int) {
        Logger.i("GameViewModel: changePlayerCount called.")
        Logger.d("GameViewModel: New player count requested: $newPlayerCount.")
        viewModelScope.launch {
            repository.changePlayerCount(newPlayerCount)
        }
    }

    /**
     * Initiates a change in the starting life total for all games.
     * This is a significant action that will cause all games to reset.
     *
     * @param newStartingLife The desired starting life total.
     */
    fun changeStartingLife(newStartingLife: Int) {
        Logger.i("GameViewModel: changeStartingLife called.")
        Logger.d("GameViewModel: New starting life requested: $newStartingLife.")
        viewModelScope.launch {
            repository.changeStartingLife(newStartingLife)
        }
    }

    /**
     * Initiates a reset for the currently active game layout only.
     * Player data and commander damage for other game sizes are unaffected.
     */
    fun resetCurrentGame() {
        Logger.i("GameViewModel: resetCurrentGame called.")
        viewModelScope.launch {
            repository.resetCurrentGame()
        }
    }

    /**
     * Initiates a full reset for all game data across all player counts.
     * This is a destructive action that reverts the entire application state
     * to its initial defaults.
     */
    fun resetAllGames() {
        Logger.i("GameViewModel: resetAllGames called.")
        viewModelScope.launch {
            repository.resetAllGames()
        }
    }

    /**
     * Increases the life total of a specific player by 1.
     *
     * @param playerIndex The 0-based index of the player whose life should be increased.
     */
    fun increaseLife(playerIndex: Int) {
        Logger.d("GameViewModel: increaseLife called for player index $playerIndex.")
        viewModelScope.launch {
            repository.increaseLife(playerIndex)
        }
    }

    /**
     * Decreases the life total of a specific player by 1.
     *
     * @param playerIndex The 0-based index of the player whose life should be decreased.
     */
    fun decreaseLife(playerIndex: Int) {
        Logger.d("GameViewModel: decreaseLife called for player index $playerIndex.")
        viewModelScope.launch {
            repository.decreaseLife(playerIndex)
        }
    }

    /**
     * Assigns a selected profile to a specific player.
     *
     * @param playerIndex The index of the player to whom the profile will be assigned.
     * @param profile The [Profile] object to assign.
     */
    fun setPlayerProfile(playerIndex: Int, profile: Profile) {
        Logger.i("GameViewModel: setPlayerProfile called.")
        Logger.d("GameViewModel: Assigning profile '${profile.nickname}' to player $playerIndex.")
        viewModelScope.launch {
            repository.updatePlayerProfile(playerIndex, profile)
        }
    }

    /**
     * Removes a profile assignment from a player, reverting them to their default state.
     *
     * @param playerIndex The index of the player from whom the profile will be unloaded.
     */
    fun unloadProfile(playerIndex: Int) {
        Logger.i("GameViewModel: unloadProfile called for player $playerIndex.")
        viewModelScope.launch {
            repository.unloadPlayerProfile(playerIndex)
        }
    }

    /**
     * Retrieves a reactive flow of commander damage dealt *to* a specific player.
     *
     * @param targetPlayerIndex The index of the player receiving the damage.
     * @return A [Flow] that emits the list of [CommanderDamage] dealt to that player.
     */
    fun getCommanderDamageForPlayer(targetPlayerIndex: Int): Flow<List<CommanderDamage>> {
        Logger.d("GameViewModel: getCommanderDamageForPlayer called for target index $targetPlayerIndex.")
        return repository.getCommanderDamageForPlayer(targetPlayerIndex)
    }

    /**
     * Increments the commander damage from one player to another.
     *
     * @param sourcePlayerIndex The index of the player dealing the damage.
     * @param targetPlayerIndex The index of the player receiving the damage.
     */
    fun incrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        Logger.d("GameViewModel: incrementCommanderDamage called. Source: $sourcePlayerIndex, Target: $targetPlayerIndex.")
        viewModelScope.launch {
            repository.incrementCommanderDamage(sourcePlayerIndex, targetPlayerIndex)
        }
    }

    /**
     * Decrements the commander damage from one player to another.
     *
     * @param sourcePlayerIndex The index of the player who dealt the damage.
     * @param targetPlayerIndex The index of the player who received the damage.
     */
    fun decrementCommanderDamage(sourcePlayerIndex: Int, targetPlayerIndex: Int) {
        Logger.d("GameViewModel: decrementCommanderDamage called. Source: $sourcePlayerIndex, Target: $targetPlayerIndex.")
        viewModelScope.launch {
            repository.decrementCommanderDamage(sourcePlayerIndex, targetPlayerIndex)
        }
    }
}