package com.example.mtglifetracker.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GameRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockPlayerDao: PlayerDao
    private lateinit var mockSettingsDao: GameSettingsDao
    private lateinit var mockProfileDao: ProfileDao
    private lateinit var repository: GameRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockPlayerDao = mock()
        mockSettingsDao = mock()
        mockProfileDao = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun repositoryInit_shouldLoadInitialData() = runTest {
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        val players = listOf(Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40))
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getPlayers(2)).thenReturn(flowOf(players))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)

        repository.gameState.test {
            skipItems(1)
            val state = awaitItem()

            assertEquals(2, state.playerCount)
            assertEquals(1, state.players.size)
            assertEquals("Player 1", state.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changePlayerCount_shouldSaveNewSettingsAndCreatePlayers() = runTest {
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)

        repository.changePlayerCount(4)
        advanceUntilIdle()

        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(4, settingsCaptor.firstValue.playerCount)

        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(4, playersCaptor.firstValue.size)
    }

    @Test
    fun increaseLife_shouldUpdatePlayerInDao() = runTest {
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Test", life = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings()))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)
        advanceUntilIdle()

        repository.increaseLife(0)
        advanceUntilIdle()

        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life)
    }

    @Test
    fun resetCurrentGame_shouldRecreatePlayersForCurrentGameSize() = runTest {
        val settings = GameSettings(playerCount = 2, startingLife = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(settings))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)

        repository.resetCurrentGame()
        advanceUntilIdle()

        verify(mockPlayerDao).deletePlayersForGame(2)
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)
    }

    @Test
    fun resetAllGames_shouldDeleteAllAndRecreatePlayers() = runTest {
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)

        repository.resetAllGames()
        advanceUntilIdle()

        verify(mockPlayerDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
    }

    @Test
    fun changeStartingLife_shouldSaveSettingsAndResetAllGames() = runTest {
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2, startingLife = 40)))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(emptyList()))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)

        repository.changeStartingLife(20)
        advanceUntilIdle()

        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(20, settingsCaptor.firstValue.startingLife)

        verify(mockPlayerDao).deleteAll()
        verify(mockPlayerDao, atLeastOnce()).insertAll(any())
    }

    @Test
    fun updatePlayerProfile_shouldUpdatePlayerInDao() = runTest {
        val initialPlayer = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings()))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)
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
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings()))
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(listOf(initialPlayer)))
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(emptyList()))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, this)
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
        // Arrange
        val backgroundScope = TestScope(testDispatcher)
        val playerWithProfile = Player(gameSize = 2, playerIndex = 0, name = "Old Name", life = 40, profileId = 1L)
        val profileFlow = MutableStateFlow(listOf(Profile(id = 1L, nickname = "Old Name", color = null)))

        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getPlayers(2)).thenReturn(flowOf(listOf(playerWithProfile)))
        whenever(mockProfileDao.getAll()).thenReturn(profileFlow)
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(playerWithProfile)))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, backgroundScope)
        backgroundScope.advanceUntilIdle()

        // Act
        val updatedProfile = Profile(id = 1L, nickname = "Updated Name", color = "#ABCDEF")
        profileFlow.value = listOf(updatedProfile)
        backgroundScope.advanceUntilIdle()

        // Assert
        val playerListCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).updatePlayers(playerListCaptor.capture())
        val capturedUpdatedPlayer = playerListCaptor.firstValue.first()
        assertEquals("Updated Name", capturedUpdatedPlayer.name)
        assertEquals("#ABCDEF", capturedUpdatedPlayer.color)
    }

    @Test
    fun profileDelete_triggersPlayerUnloadInRepository() = runTest {
        // Arrange
        val backgroundScope = TestScope(testDispatcher)
        val playerWithProfile = Player(gameSize = 2, playerIndex = 0, name = "Some Name", life = 40, profileId = 1L)
        val profileFlow = MutableStateFlow(listOf(Profile(id = 1L, nickname = "Some Name", color = null)))

        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(GameSettings(playerCount = 2)))
        whenever(mockPlayerDao.getPlayers(2)).thenReturn(flowOf(listOf(playerWithProfile)))
        whenever(mockProfileDao.getAll()).thenReturn(profileFlow)
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(listOf(playerWithProfile)))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, backgroundScope)
        backgroundScope.advanceUntilIdle()

        // Act
        profileFlow.value = emptyList()
        backgroundScope.advanceUntilIdle()

        // Assert
        val playerListCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).updatePlayers(playerListCaptor.capture())
        val updatedPlayer = playerListCaptor.firstValue.first()
        assertEquals("Player 1", updatedPlayer.name)
        assertNull(updatedPlayer.profileId)
    }
}