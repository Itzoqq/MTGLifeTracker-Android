package com.example.mtglifetracker.data

import app.cash.turbine.test
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.model.Profile
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.atLeast
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GameRepositoryTest {

    private lateinit var mockPlayerDao: PlayerDao
    private lateinit var mockSettingsDao: GameSettingsDao
    private lateinit var mockProfileDao: ProfileDao // Add mock for ProfileDao
    private lateinit var repository: GameRepository
    private val testScope = TestScope()

    private val defaultPlayers = listOf(
        Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40),
        Player(gameSize = 2, playerIndex = 1, name = "Player 2", life = 40)
    )

    // Add a default empty profiles list for mocking
    private val defaultProfiles = emptyList<Profile>()

    @Before
    fun setup() {
        mockPlayerDao = mock()
        mockSettingsDao = mock()
        mockProfileDao = mock() // Initialize the mock
        val initialSettings = GameSettings(playerCount = 2, startingLife = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(initialSettings))
        // Mock the new DAO calls to return empty flows by default
        whenever(mockProfileDao.getAll()).thenReturn(flowOf(defaultProfiles))
        whenever(mockPlayerDao.getAllPlayers()).thenReturn(flowOf(defaultPlayers))
    }

    // Update the helper to pass the new dependency
    private fun initializeRepositoryWithPlayers(initialPlayers: List<Player>) {
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(initialPlayers))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, testScope)
    }

    @Test
    fun repositoryInit_shouldLoadInitialDataAndGameStateShouldReflectIt() = testScope.runTest {
        initializeRepositoryWithPlayers(defaultPlayers)
        advanceUntilIdle()

        repository.gameState.test {
            val emittedState = awaitItem()
            assertEquals(2, emittedState.playerCount)
            assertEquals(2, emittedState.players.size)
            assertEquals("Player 1", emittedState.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changePlayerCount_shouldSaveNewSettingsAndCreateNewPlayersIfNeeded() = testScope.runTest {
        val newPlayerCount = 4
        initializeRepositoryWithPlayers(defaultPlayers)
        whenever(mockPlayerDao.getPlayers(newPlayerCount)).thenReturn(flowOf(emptyList()))
        advanceUntilIdle()

        repository.changePlayerCount(newPlayerCount)
        advanceUntilIdle()

        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(newPlayerCount, settingsCaptor.firstValue.playerCount)

        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(newPlayerCount, playersCaptor.firstValue.size)
    }

    @Test
    fun increaseLife_shouldUpdatePlayerInDaoAndReflectInGameState() = testScope.runTest {
        initializeRepositoryWithPlayers(defaultPlayers)
        advanceUntilIdle()

        repository.increaseLife(playerIndex = 0)
        advanceUntilIdle()

        val finalState = repository.gameState.value
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life)
        assertEquals(41, finalState.players[0].life)
        // REMOVED: Assertion for playerDeltas
    }

    @Test
    fun decreaseLife_shouldUpdatePlayerInDaoAndReflectInGameState() = testScope.runTest {
        initializeRepositoryWithPlayers(defaultPlayers)
        advanceUntilIdle()

        repository.decreaseLife(playerIndex = 1)
        advanceUntilIdle()

        val finalState = repository.gameState.value
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(39, playerCaptor.firstValue.life)
        assertEquals(39, finalState.players[1].life)
        // REMOVED: Assertion for playerDeltas
    }

    @Test
    fun resetCurrentGame_shouldDeleteAndRecreatePlayersForCurrentGameSize() = testScope.runTest {
        val currentGameSize = 2
        val initialPlayers = listOf(Player(gameSize = currentGameSize, playerIndex = 0, life = 40))

        whenever(mockPlayerDao.getPlayers(currentGameSize))
            .thenReturn(flowOf(initialPlayers))
            .thenReturn(flowOf(emptyList()))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, testScope)
        advanceUntilIdle()

        repository.resetCurrentGame()
        advanceUntilIdle()

        verify(mockPlayerDao).deletePlayersForGame(currentGameSize)
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(currentGameSize, playersCaptor.firstValue.size)
    }

    @Test
    fun resetAllGames_shouldDeleteAllPlayersAndRecreateForCurrentGameSize() = testScope.runTest {
        val initialPlayers = listOf(Player(gameSize = 2, playerIndex = 0, life = 40))

        whenever(mockPlayerDao.getPlayers(any()))
            .thenReturn(flowOf(initialPlayers))
            .thenReturn(flowOf(emptyList()))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, testScope)
        advanceUntilIdle()

        repository.resetAllGames()
        advanceUntilIdle()

        verify(mockPlayerDao).deleteAll()
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao, atLeast(1)).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)
    }

    // REMOVED: The resetDeltaForPlayer test is no longer relevant to the repository.

    @Test
    fun changeStartingLife_shouldSaveNewSettingsAndRecreatePlayersWithNewLife() = testScope.runTest {
        val newStartingLife = 20
        val initialPlayers = listOf(Player(gameSize = 2, playerIndex = 0, life = 40))
        val initialSettings = GameSettings(playerCount = 2, startingLife = 40)
        val newSettings = GameSettings(playerCount = 2, startingLife = newStartingLife)

        whenever(mockPlayerDao.getPlayers(any()))
            .thenReturn(flowOf(initialPlayers))
            .thenReturn(flowOf(emptyList()))

        whenever(mockSettingsDao.getSettings())
            .thenReturn(flowOf(initialSettings))
            .thenReturn(flowOf(newSettings))

        // Pass the new mock dependency here
        repository = GameRepository(mockPlayerDao, mockSettingsDao, mockProfileDao, testScope)
        advanceUntilIdle()

        repository.changeStartingLife(newStartingLife)
        advanceUntilIdle()

        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(newStartingLife, settingsCaptor.firstValue.startingLife)

        verify(mockPlayerDao).deleteAll()
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao, atLeast(1)).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)
        assertEquals(newStartingLife, playersCaptor.firstValue[0].life)
    }
}