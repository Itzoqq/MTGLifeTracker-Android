package com.example.mtglifetracker.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Local unit tests for the [GameRepository].
 *
 * This class verifies the business logic within the repository. It uses a mocking framework (Mockito)
 * to provide fake implementations of the DAOs, allowing the tests to isolate the repository's
 * logic from the actual database. It also uses `runTest` and a [TestScope] to control the
 * execution of coroutines in a predictable and testable way.
 */
@ExperimentalCoroutinesApi
class GameRepositoryTest {

    // This rule swaps the background executor used by Architecture Components with a
    // different one that executes each task synchronously.
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // A test dispatcher for controlling coroutine execution.
    private val testDispatcher = StandardTestDispatcher()
    // A dedicated scope for the Repository to run its coroutines, allowing for proper cancellation.
    private lateinit var repositoryScope: TestScope

    // --- Mocks for all DAO dependencies ---
    private lateinit var mockPlayerDao: PlayerDao
    private lateinit var mockSettingsDao: GameSettingsDao
    private lateinit var mockProfileDao: ProfileDao
    private lateinit var mockCommanderDamageDao: CommanderDamageDao
    private lateinit var mockPreferencesDao: PreferencesDao
    private lateinit var repository: GameRepository

    /**
     * Sets up the test environment before each test.
     * This method initializes the main dispatcher for coroutines, creates a new [TestScope],
     * and initializes all the mock DAO instances.
     */
    @Before
    fun setup() {
        Logger.unit("TEST_SETUP: Starting...")
        Dispatchers.setMain(testDispatcher)
        repositoryScope = TestScope(testDispatcher)

        // Create mock instances for all DAO dependencies.
        mockPlayerDao = mock()
        mockSettingsDao = mock()
        mockProfileDao = mock()
        mockCommanderDamageDao = mock()
        mockPreferencesDao = mock()

        // Provide default empty flows for DAOs that are always collected during repository init.
        whenever(mockCommanderDamageDao.getAllDamage()).thenReturn(flowOf(emptyList()))
        whenever(mockPreferencesDao.getPreferences()).thenReturn(flowOf(Preferences()))
        Logger.unit("TEST_SETUP: Complete.")
    }

    /**
     * Cleans up the test environment after each test.
     * This method cancels the repository's coroutine scope and resets the main dispatcher.
     */
    @After
    fun tearDown() {
        Logger.unit("TEST_TEARDOWN: Starting...")
        // Cancel the dedicated scope to clean up any running coroutines in the repository.
        repositoryScope.cancel()
        // Reset the main dispatcher to its original state.
        Dispatchers.resetMain()
        Logger.unit("TEST_TEARDOWN: Complete.")
    }

    /**
     * Tests that the repository correctly initializes its game state by loading data
     * from the DAOs when it is first created.
     */
    @Test
    fun repositoryInit_shouldLoadInitialData() = runTest {
        Logger.unit("TEST_START: repositoryInit_shouldLoadInitialData")
        // Arrange
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        val players = listOf(Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40))
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(players))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        Logger.unit("Arrange: Mock DAOs prepared with initial data.")

        // Act
        Logger.unit("Act: Instantiating GameRepository.")
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )

        // Assert
        Logger.unit("Assert: Testing the gameState flow for correct initial state.")
        repository.gameState.test {
            // The flow starts with a default empty state, so we skip it.
            skipItems(1)
            val state = awaitItem()
            Logger.unit("Assert: Received state: $state")
            assertEquals(2, state.playerCount)
            assertEquals(1, state.players.size)
            assertEquals("Player 1", state.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
        Logger.unit("TEST_PASS: repositoryInit_shouldLoadInitialData")
    }

    /**
     * Tests that changing the player count correctly saves the new settings and initializes
     * the appropriate number of new player entries.
     */
    @Test
    fun changePlayerCount_shouldSaveNewSettingsAndCreatePlayers() = runTest {
        Logger.unit("TEST_START: changePlayerCount_shouldSaveNewSettingsAndCreatePlayers")
        // Arrange
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle() // Ensure repository is initialized.
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Calling changePlayerCount(4).")
        repository.changePlayerCount(4)
        advanceUntilIdle() // Execute the coroutine.

        // Assert
        Logger.unit("Assert: Verifying that saveSettings and insertAll were called with correct values.")
        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(4, settingsCaptor.firstValue.playerCount)

        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(4, playersCaptor.firstValue.size)
        Logger.unit("TEST_PASS: changePlayerCount_shouldSaveNewSettingsAndCreatePlayers")
    }

    /**
     * Tests that increasing a player's life correctly calls the update method in the DAO.
     */
    @Test
    fun increaseLife_shouldUpdatePlayerInDao() = runTest {
        Logger.unit("TEST_START: increaseLife_shouldUpdatePlayerInDao")
        // Arrange
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Test", life = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized with one player at 40 life.")

        // Act
        Logger.unit("Act: Calling increaseLife(0).")
        repository.increaseLife(0)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying updatePlayer was called with life 41.")
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life)
        Logger.unit("TEST_PASS: increaseLife_shouldUpdatePlayerInDao")
    }

    @Test
    fun resetCurrentGame_shouldRecreatePlayersForCurrentGameSize() = runTest {
        Logger.unit("TEST_START: resetCurrentGame_shouldRecreatePlayersForCurrentGameSize")
        // Arrange
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Calling resetCurrentGame.")
        repository.resetCurrentGame()
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying players and damage were deleted and then re-inserted for game size 2.")
        verify(mockPlayerDao).deletePlayersForGame(2)
        verify(mockCommanderDamageDao).deleteCommanderDamageForGame(2)
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)
        Logger.unit("TEST_PASS: resetCurrentGame_shouldRecreatePlayersForCurrentGameSize")
    }

    @Test
    fun resetAllGames_shouldDeleteAllAndRecreatePlayers() = runTest {
        Logger.unit("TEST_START: resetAllGames_shouldDeleteAllAndRecreatePlayers")
        // Arrange
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Calling resetAllGames.")
        repository.resetAllGames()
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying all players and damage were deleted, then players re-inserted.")
        verify(mockPlayerDao).deleteAll()
        verify(mockCommanderDamageDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
        Logger.unit("TEST_PASS: resetAllGames_shouldDeleteAllAndRecreatePlayers")
    }

    @Test
    fun changeStartingLife_shouldSaveSettingsAndResetAllGames() = runTest {
        Logger.unit("TEST_START: changeStartingLife_shouldSaveSettingsAndResetAllGames")
        // Arrange
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Calling changeStartingLife(20).")
        repository.changeStartingLife(20)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying settings saved, all players deleted, and new players inserted.")
        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(20, settingsCaptor.firstValue.startingLife)
        verify(mockPlayerDao).deleteAll()
        verify(mockCommanderDamageDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
        Logger.unit("TEST_PASS: changeStartingLife_shouldSaveSettingsAndResetAllGames")
    }

    @Test
    fun updatePlayerProfile_shouldUpdatePlayerInDao() = runTest {
        Logger.unit("TEST_START: updatePlayerProfile_shouldUpdatePlayerInDao")
        // Arrange
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized.")
        val profileToSet = Profile(id = 1L, nickname = "New Profile", color = "#FFFFFF")

        // Act
        Logger.unit("Act: Calling updatePlayerProfile.")
        repository.updatePlayerProfile(0, profileToSet)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying player was updated with new profile name.")
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals("New Profile", playerCaptor.firstValue.name)
        Logger.unit("TEST_PASS: updatePlayerProfile_shouldUpdatePlayerInDao")
    }

    @Test
    fun unloadPlayerProfile_shouldUpdatePlayerToDefaultsInDao() = runTest {
        Logger.unit("TEST_START: unloadPlayerProfile_shouldUpdatePlayerToDefaultsInDao")
        // Arrange
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40, profileId = 1L)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Calling unloadPlayerProfile.")
        repository.unloadPlayerProfile(0)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying player was updated to default name and null profile ID.")
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals("Player 1", playerCaptor.firstValue.name)
        assertNull(playerCaptor.firstValue.profileId)
        Logger.unit("TEST_PASS: unloadPlayerProfile_shouldUpdatePlayerToDefaultsInDao")
    }

    @Test
    fun profileUpdate_triggersPlayerUpdateInRepository() = runTest {
        Logger.unit("TEST_START: profileUpdate_triggersPlayerUpdateInRepository")
        // Arrange
        val playerWithProfile = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40, profileId = 1L)
        val allPlayersFlow = MutableStateFlow(listOf(playerWithProfile))
        val profileFlow = MutableStateFlow(listOf(Profile(id = 1L, nickname = "Old Name", color = null)))
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(allPlayersFlow)
        whenever(mockProfileDao.getAll()).thenReturn(profileFlow)
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Emitting an updated profile from the profile flow.")
        val updatedProfile = Profile(id = 1L, nickname = "Updated Name", color = "#ABCDEF")
        profileFlow.value = listOf(updatedProfile)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying player was automatically updated to match the new profile data.")
        val playerListCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).updatePlayers(playerListCaptor.capture())
        val capturedUpdatedPlayer = playerListCaptor.firstValue.first()
        assertEquals("Updated Name", capturedUpdatedPlayer.name)
        assertEquals("#ABCDEF", capturedUpdatedPlayer.color)
        Logger.unit("TEST_PASS: profileUpdate_triggersPlayerUpdateInRepository")
    }

    @Test
    fun profileDelete_triggersPlayerUnloadInRepository() = runTest {
        Logger.unit("TEST_START: profileDelete_triggersPlayerUnloadInRepository")
        // Arrange
        val playerWithProfile = Player(gameSize = 2, playerIndex = 0, name = "Some Name", life = 40, profileId = 1L)
        val allPlayersFlow = MutableStateFlow(listOf(playerWithProfile))
        val profileFlow = MutableStateFlow(listOf(Profile(id = 1L, nickname = "Some Name", color = null)))
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(allPlayersFlow)
        whenever(mockProfileDao.getAll()).thenReturn(profileFlow)
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized.")

        // Act
        Logger.unit("Act: Emitting an empty list from the profile flow, simulating deletion.")
        profileFlow.value = emptyList()
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying player was automatically reverted to default state.")
        val playerListCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).updatePlayers(playerListCaptor.capture())
        val updatedPlayer = playerListCaptor.firstValue.first()
        assertEquals("Player 1", updatedPlayer.name)
        assertNull(updatedPlayer.profileId)
        Logger.unit("TEST_PASS: profileDelete_triggersPlayerUnloadInRepository")
    }

    @Test
    fun incrementCommanderDamage_withDeduction_updatesPlayerLife() = runTest {
        Logger.unit("TEST_START: incrementCommanderDamage_withDeduction_updatesPlayerLife")
        // Arrange
        val allPlayersInGame = listOf(
            Player(gameSize = 2, playerIndex = 0, name = "Source", life = 40),
            Player(gameSize = 2, playerIndex = 1, name = "Target", life = 40)
        )
        val allPlayersFlow = MutableStateFlow(allPlayersInGame)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(allPlayersFlow)
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        whenever(mockPreferencesDao.getPreferences()).thenReturn(flowOf(Preferences(deduceCommanderDamage = true)))
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized with damage deduction enabled.")

        // Act
        Logger.unit("Act: Calling incrementCommanderDamage.")
        repository.incrementCommanderDamage(0, 1)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying damage was incremented and life was deducted.")
        verify(mockCommanderDamageDao).incrementCommanderDamage(2, 0, 1)
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(39, playerCaptor.firstValue.life)
        Logger.unit("TEST_PASS: incrementCommanderDamage_withDeduction_updatesPlayerLife")
    }

    @Test
    fun decrementCommanderDamage_withDeduction_updatesPlayerLife() = runTest {
        Logger.unit("TEST_START: decrementCommanderDamage_withDeduction_updatesPlayerLife")
        // Arrange
        val allPlayersInGame = listOf(
            Player(gameSize = 2, playerIndex = 0, name = "Source", life = 40),
            Player(gameSize = 2, playerIndex = 1, name = "Target", life = 30)
        )
        val allPlayersFlow = MutableStateFlow(allPlayersInGame)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(allPlayersFlow)
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getAllDamage()).thenReturn(flowOf(listOf(CommanderDamage(2, 0, 1, 5))))
        whenever(mockPreferencesDao.getPreferences()).thenReturn(flowOf(Preferences(deduceCommanderDamage = true)))
        whenever(mockCommanderDamageDao.getDamageValue(2, 0, 1)).thenReturn(5)
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        Logger.unit("Arrange: Repository initialized with damage deduction enabled.")

        // Act
        Logger.unit("Act: Calling decrementCommanderDamage.")
        repository.decrementCommanderDamage(0, 1)
        advanceUntilIdle()

        // Assert
        Logger.unit("Assert: Verifying damage was decremented and life was added back.")
        verify(mockCommanderDamageDao).decrementCommanderDamage(2, 0, 1)
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(31, playerCaptor.firstValue.life)
        Logger.unit("TEST_PASS: decrementCommanderDamage_withDeduction_updatesPlayerLife")
    }
}