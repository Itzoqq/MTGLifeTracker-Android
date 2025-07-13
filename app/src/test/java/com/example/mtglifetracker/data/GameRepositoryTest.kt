package com.example.mtglifetracker.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Preferences
import com.example.mtglifetracker.model.Profile
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

@ExperimentalCoroutinesApi
class GameRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repositoryScope: TestScope // Scope for the Repository

    private lateinit var mockPlayerDao: PlayerDao
    private lateinit var mockSettingsDao: GameSettingsDao
    private lateinit var mockProfileDao: ProfileDao
    private lateinit var mockCommanderDamageDao: CommanderDamageDao
    private lateinit var mockPreferencesDao: PreferencesDao
    private lateinit var repository: GameRepository

    @Before
    fun setup() {
        println("\n--- TEST_SETUP ---")
        Dispatchers.setMain(testDispatcher)
        repositoryScope = TestScope(testDispatcher) // Create the dedicated scope

        mockPlayerDao = mock()
        mockSettingsDao = mock()
        mockProfileDao = mock()
        mockCommanderDamageDao = mock()
        mockPreferencesDao = mock()

        whenever(mockCommanderDamageDao.getAllDamage()).thenReturn(flowOf(emptyList()))
        whenever(mockPreferencesDao.getPreferences()).thenReturn(flowOf(Preferences()))
        println("--- END_TEST_SETUP ---\n")
    }

    @After
    fun tearDown() {
        println("\n--- TEST_TEARDOWN ---")
        repositoryScope.cancel() // Cancel the repository's scope
        Dispatchers.resetMain()
        println("--- END_TEST_TEARDOWN ---\n")
    }

    @Test
    fun repositoryInit_shouldLoadInitialData() = runTest {
        println("TEST_START: repositoryInit_shouldLoadInitialData")
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        val players = listOf(Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40))
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(players))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)

        println("TEST_LOG: Instantiating Repository with its own scope")
        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )

        repository.gameState.test {
            println("TEST_LOG: Skipping initial empty state")
            skipItems(1)
            println("TEST_LOG: Awaiting item from gameState flow")
            val state = awaitItem()
            println("TEST_LOG: Received state: $state")

            assertEquals(2, state.playerCount)
            assertEquals(1, state.players.size)
            assertEquals("Player 1", state.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
        println("TEST_END: repositoryInit_shouldLoadInitialData")
    }

    @Test
    fun changePlayerCount_shouldSaveNewSettingsAndCreatePlayers() = runTest {
        println("TEST_START: changePlayerCount_shouldSaveNewSettingsAndCreatePlayers")
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()

        println("TEST_LOG: Calling changePlayerCount(4)")
        repository.changePlayerCount(4)
        advanceUntilIdle()
        println("TEST_LOG: Coroutines idle")

        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(4, settingsCaptor.firstValue.playerCount)

        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(4, playersCaptor.firstValue.size)
        println("TEST_END: changePlayerCount_shouldSaveNewSettingsAndCreatePlayers")
    }

    @Test
    fun increaseLife_shouldUpdatePlayerInDao() = runTest {
        println("TEST_START: increaseLife_shouldUpdatePlayerInDao")
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Test", life = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()

        repository.increaseLife(0)
        advanceUntilIdle()

        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life)
        println("TEST_END: increaseLife_shouldUpdatePlayerInDao")
    }

    // ... all other 7 passing tests remain here ...

    @Test
    fun resetCurrentGame_shouldRecreatePlayersForCurrentGameSize() = runTest {
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )

        repository.resetCurrentGame()
        advanceUntilIdle()

        verify(mockPlayerDao).deletePlayersForGame(2)
        verify(mockCommanderDamageDao).deleteCommanderDamageForGame(2)
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)
    }

    @Test
    fun resetAllGames_shouldDeleteAllAndRecreatePlayers() = runTest {
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )

        repository.resetAllGames()
        advanceUntilIdle()

        verify(mockPlayerDao).deleteAll()
        verify(mockCommanderDamageDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
    }

    @Test
    fun changeStartingLife_shouldSaveSettingsAndResetAllGames() = runTest {
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(0)
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )

        repository.changeStartingLife(20)
        advanceUntilIdle()

        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(20, settingsCaptor.firstValue.startingLife)

        verify(mockPlayerDao).deleteAll()
        verify(mockCommanderDamageDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
    }

    @Test
    fun updatePlayerProfile_shouldUpdatePlayerInDao() = runTest {
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()
        val profileToSet = Profile(id = 1L, nickname = "New Profile", color = "#FFFFFF")

        repository.updatePlayerProfile(0, profileToSet)
        advanceUntilIdle()

        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals("New Profile", playerCaptor.firstValue.name)
    }

    @Test
    fun unloadPlayerProfile_shouldUpdatePlayerToDefaultsInDao() = runTest {
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40, profileId = 1L)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockCommanderDamageDao.getDamageEntryCountForGame(any())).thenReturn(2)

        repository = GameRepository(
            mockPlayerDao, mockSettingsDao, mockProfileDao, mockCommanderDamageDao, mockPreferencesDao, repositoryScope
        )
        advanceUntilIdle()

        repository.unloadPlayerProfile(0)
        advanceUntilIdle()

        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals("Player 1", playerCaptor.firstValue.name)
        assertNull(playerCaptor.firstValue.profileId)
    }

    @Test
    fun profileUpdate_triggersPlayerUpdateInRepository() = runTest {
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

        val updatedProfile = Profile(id = 1L, nickname = "Updated Name", color = "#ABCDEF")
        profileFlow.value = listOf(updatedProfile)
        advanceUntilIdle()

        val playerListCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).updatePlayers(playerListCaptor.capture())
        val capturedUpdatedPlayer = playerListCaptor.firstValue.first()
        assertEquals("Updated Name", capturedUpdatedPlayer.name)
        assertEquals("#ABCDEF", capturedUpdatedPlayer.color)
    }

    @Test
    fun profileDelete_triggersPlayerUnloadInRepository() = runTest {
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

        profileFlow.value = emptyList()
        advanceUntilIdle()

        val playerListCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).updatePlayers(playerListCaptor.capture())
        val updatedPlayer = playerListCaptor.firstValue.first()
        assertEquals("Player 1", updatedPlayer.name)
        assertNull(updatedPlayer.profileId)
    }

    @Test
    fun incrementCommanderDamage_withDeduction_updatesPlayerLife() = runTest {
        println("TEST_START: incrementCommanderDamage_withDeduction_updatesPlayerLife")
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
        println("TEST_LOG: Repository initialized and idle.")

        repository.incrementCommanderDamage(0, 1)
        advanceUntilIdle()
        println("TEST_LOG: Coroutines idle after increment.")

        verify(mockCommanderDamageDao).incrementCommanderDamage(2, 0, 1)
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(39, playerCaptor.firstValue.life)
        println("TEST_END: incrementCommanderDamage_withDeduction_updatesPlayerLife")
    }

    @Test
    fun decrementCommanderDamage_withDeduction_updatesPlayerLife() = runTest {
        println("TEST_START: decrementCommanderDamage_withDeduction_updatesPlayerLife")
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
        println("TEST_LOG: Repository initialized and idle.")

        repository.decrementCommanderDamage(0, 1)
        advanceUntilIdle()
        println("TEST_LOG: Coroutines idle after decrement.")

        verify(mockCommanderDamageDao).decrementCommanderDamage(2, 0, 1)
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(31, playerCaptor.firstValue.life)
        println("TEST_END: decrementCommanderDamage_withDeduction_updatesPlayerLife")
    }
}