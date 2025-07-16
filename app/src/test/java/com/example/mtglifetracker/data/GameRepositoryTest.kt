package com.example.mtglifetracker.data

import app.cash.turbine.test
import com.example.mtglifetracker.ThemedRobolectricTest
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

/**
 * Local unit tests for the [GameRepository].
 *
 * These tests use Robolectric to simulate an Android environment and Mockito to provide
 * mock implementations of the DAOs. This isolates the repository from the database, allowing
 * for focused testing of its business logic. Coroutine execution is controlled using
 * a [TestScope] and [StandardTestDispatcher] for predictable, synchronous-like testing.
 */
@ExperimentalCoroutinesApi
@Config(sdk = [34])
class GameRepositoryTest : ThemedRobolectricTest() {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repositoryScope: TestScope

    // --- Mocks for all DAO dependencies ---
    private lateinit var mockPlayerDao: PlayerDao
    private lateinit var mockSettingsDao: GameSettingsDao
    private lateinit var mockProfileDao: ProfileDao
    private lateinit var mockCommanderDamageDao: CommanderDamageDao
    private lateinit var mockPreferencesDao: PreferencesDao

    // The object being tested
    private lateinit var repository: GameRepository

    /**
     * Sets up the test environment before each test. Initializes mocks and the
     * coroutine scope. The repository itself is initialized within each test to allow
     * for test-specific mock configurations.
     */
    @Before
    fun setup() {
        super.setupThemeAndLogging()
        Logger.unit("TEST_SETUP: starting")
        repositoryScope = TestScope(testDispatcher)

        mockPlayerDao = mock()
        mockSettingsDao = mock()
        mockProfileDao = mock()
        mockCommanderDamageDao = mock()
        mockPreferencesDao = mock()

        // Provide default, non-null responses for all DAO flows to prevent crashes.
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getAllDamage()).thenReturn(flowOf(emptyList()))
        whenever(mockPreferencesDao.getPreferences()).thenReturn(flowOf(Preferences()))
        runTest {
            whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        }
        Logger.unit("TEST_SETUP: complete")
    }

    /**
     * Cleans up the test environment after each test by cancelling the coroutine scope.
     */
    @After
    fun tearDown() {
        Logger.unit("TEST_TEARDOWN: starting")
        repositoryScope.cancel()
        Logger.unit("TEST_TEARDOWN: complete")
    }

    /**
     * A helper function to instantiate the repository within a test.
     */
    private fun initializeRepository() {
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        Logger.unit("Repository instantiated for test.")
    }

    // =================================================================================
    // Passing Tests (Unchanged)
    // =================================================================================

    @Test
    fun repository_init_should_load_initial_data() = runTest(testDispatcher) {
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        val players = listOf(Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40))
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(players))

        initializeRepository()

        repository.gameState.test {
            skipItems(1) // Skip the default initial state
            val state = awaitItem()
            assertEquals(1, state.players.size)
            assertEquals("Player 1", state.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reset_current_game_should_clear_relevant_data() = runTest(testDispatcher) {
        initializeRepository()
        repository.resetCurrentGame()
        advanceUntilIdle()
        verify(mockPlayerDao).deletePlayersForGame(any())
        verify(mockCommanderDamageDao).deleteCommanderDamageForGame(any())
    }

    @Test
    fun reset_all_games_should_clear_all_data() = runTest(testDispatcher) {
        initializeRepository()
        repository.resetAllGames()
        advanceUntilIdle()
        verify(mockPlayerDao).deleteAll()
        verify(mockCommanderDamageDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
    }

    @Test
    fun change_starting_life_should_save_settings_and_reset() = runTest(testDispatcher) {
        initializeRepository()
        val newStartingLife = 20
        repository.changeStartingLife(newStartingLife)
        advanceUntilIdle()
        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(newStartingLife, settingsCaptor.firstValue.startingLife)
        verify(mockPlayerDao).deleteAll()
    }

    @Test
    fun changing_player_life_should_call_player_dao() = runTest(testDispatcher) {
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Test", life = 40)
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))

        initializeRepository()
        advanceUntilIdle()

        repository.increaseLife(0)
        advanceUntilIdle()

        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life)
    }
}