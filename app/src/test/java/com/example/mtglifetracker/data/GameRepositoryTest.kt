package com.example.mtglifetracker.data

import app.cash.turbine.test
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
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
    private lateinit var repository: GameRepository
    private val testScope = TestScope()

    // Default players list for tests that need an initial state
    private val defaultPlayers = listOf(
        Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40),
        Player(gameSize = 2, playerIndex = 1, name = "Player 2", life = 40)
    )

    @Before
    fun setup() {
        mockPlayerDao = mock()
        mockSettingsDao = mock()
        val initialSettings = GameSettings(playerCount = 2, startingLife = 40)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(initialSettings))
    }

    private fun initializeRepositoryWithPlayers(initialPlayers: List<Player>) {
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(initialPlayers))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
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
        assertEquals(1, finalState.playerDeltas[0])
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
        assertEquals(-1, finalState.playerDeltas[1])
    }

    @Test
    fun resetCurrentGame_shouldDeleteAndRecreatePlayersForCurrentGameSize() = testScope.runTest {
        val currentGameSize = 2
        val initialPlayers = listOf(Player(gameSize = currentGameSize, playerIndex = 0, life = 40))

        whenever(mockPlayerDao.getPlayers(currentGameSize))
            .thenReturn(flowOf(initialPlayers))
            .thenReturn(flowOf(emptyList()))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
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

        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
        advanceUntilIdle()

        repository.resetAllGames()
        advanceUntilIdle()

        verify(mockPlayerDao).deleteAll()
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao, atLeast(1)).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)
    }

    @Test
    fun resetDeltaForPlayer_shouldClearDeltaAndActiveStatusInGameState() = testScope.runTest {
        initializeRepositoryWithPlayers(defaultPlayers)
        advanceUntilIdle()
        repository.increaseLife(playerIndex = 0)
        advanceUntilIdle()

        repository.resetDeltaForPlayer(playerIndex = 0)

        val state = repository.gameState.value
        assertEquals(0, state.playerDeltas[0])
        assertEquals(emptySet<Int>(), state.activeDeltaPlayers)
    }

    @Test
    fun changeStartingLife_shouldSaveNewSettingsAndRecreatePlayersWithNewLife() = testScope.runTest {
        // Arrange
        val newStartingLife = 20
        val initialPlayers = listOf(Player(gameSize = 2, playerIndex = 0, life = 40))
        val initialSettings = GameSettings(playerCount = 2, startingLife = 40)
        val newSettings = GameSettings(playerCount = 2, startingLife = newStartingLife)

        // Setup for getPlayers: return initial list once, then empty lists for subsequent calls.
        whenever(mockPlayerDao.getPlayers(any()))
            .thenReturn(flowOf(initialPlayers))
            .thenReturn(flowOf(emptyList()))

        // ***THE FIX***:
        // Setup for getSettings: return initial settings on the first call (for repo init),
        // then return the new settings on the second call (for the reset).
        whenever(mockSettingsDao.getSettings())
            .thenReturn(flowOf(initialSettings))
            .thenReturn(flowOf(newSettings))

        // Initialize the repository. This consumes the first `thenReturn` from both mocks.
        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
        advanceUntilIdle()

        // Act
        repository.changeStartingLife(newStartingLife)
        advanceUntilIdle()

        // Assert
        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(newStartingLife, settingsCaptor.firstValue.startingLife)

        verify(mockPlayerDao).deleteAll()
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao, atLeast(1)).insertAll(playersCaptor.capture())
        assertEquals(2, playersCaptor.firstValue.size)

        // This assertion should now pass successfully
        assertEquals(newStartingLife, playersCaptor.firstValue[0].life)
    }
}