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

    @Before
    fun setup() {
        // Initialize mocks before each test
        mockPlayerDao = mock()
        mockSettingsDao = mock()

        // Common setup for settings, used across multiple tests
        val initialSettings = GameSettings(playerCount = 2)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(initialSettings))
    }

    private fun initializeRepositoryWithPlayers() {
        val initialPlayers = listOf(
            Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40),
            Player(gameSize = 2, playerIndex = 1, name = "Player 2", life = 40)
        )
        whenever(mockPlayerDao.getPlayers(any())).thenReturn(flowOf(initialPlayers))
        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
    }

    @Test
    fun `repository init should load initial data and gameState should reflect it`() = testScope.runTest {
        // Arrange
        initializeRepositoryWithPlayers()

        // Act
        advanceUntilIdle()

        // Assert
        repository.gameState.test {
            val emittedState = awaitItem()
            assertEquals(2, emittedState.playerCount)
            assertEquals(2, emittedState.players.size)
            assertEquals("Player 1", emittedState.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changePlayerCount should save new settings and create new players if needed`() = testScope.runTest {
        // Arrange
        val newPlayerCount = 4
        // Start with existing players for the old count
        initializeRepositoryWithPlayers()
        // Simulate that no players exist for the new count
        whenever(mockPlayerDao.getPlayers(newPlayerCount)).thenReturn(flowOf(emptyList()))
        advanceUntilIdle()

        // Act
        repository.changePlayerCount(newPlayerCount)
        advanceUntilIdle()

        // Assert
        val settingsCaptor = argumentCaptor<GameSettings>()
        verify(mockSettingsDao).saveSettings(settingsCaptor.capture())
        assertEquals(newPlayerCount, settingsCaptor.firstValue.playerCount)

        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(newPlayerCount, playersCaptor.firstValue.size)
    }

    @Test
    fun `increaseLife should update player in DAO and reflect in gameState`() = testScope.runTest {
        // Arrange
        initializeRepositoryWithPlayers()
        advanceUntilIdle()

        // Act
        repository.increaseLife(playerIndex = 0)
        advanceUntilIdle()

        // Assert
        val finalState = repository.gameState.value
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life)
        assertEquals(41, finalState.players[0].life)
        assertEquals(1, finalState.playerDeltas[0])
    }

    @Test
    fun `decreaseLife should update player in DAO and reflect in gameState`() = testScope.runTest {
        // Arrange
        initializeRepositoryWithPlayers()
        advanceUntilIdle()

        // Act
        repository.decreaseLife(playerIndex = 1)
        advanceUntilIdle()

        // Assert
        val finalState = repository.gameState.value
        val playerCaptor = argumentCaptor<Player>()
        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(39, playerCaptor.firstValue.life)
        assertEquals(39, finalState.players[1].life)
        assertEquals(-1, finalState.playerDeltas[1])
    }

    @Test
    fun `resetCurrentGame should delete and recreate players for the current game size`() = testScope.runTest {
        // Arrange
        val currentGameSize = 2
        val initialPlayers = listOf(Player(gameSize = currentGameSize, playerIndex = 0))

        // **THE FIX IS HERE**
        // We tell Mockito to return a list of players for the initial setup,
        // and then an empty list for the check that happens inside ensurePlayersExistForGameSize.
        whenever(mockPlayerDao.getPlayers(currentGameSize))
            .thenReturn(flowOf(initialPlayers)) // First call for init
            .thenReturn(flowOf(emptyList()))     // Second call after deletion

        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
        advanceUntilIdle() // Let repository initialize

        // Act
        repository.resetCurrentGame()
        advanceUntilIdle()

        // Assert
        verify(mockPlayerDao).deletePlayersForGame(currentGameSize)
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(currentGameSize, playersCaptor.firstValue.size)
    }

    @Test
    fun `resetAllGames should delete all players and recreate for current game size`() = testScope.runTest {
        // Arrange
        val currentGameSize = 2
        val initialPlayers = listOf(Player(gameSize = currentGameSize, playerIndex = 0))

        // **THE FIX IS HERE** (Same logic as resetCurrentGame)
        whenever(mockPlayerDao.getPlayers(currentGameSize))
            .thenReturn(flowOf(initialPlayers))
            .thenReturn(flowOf(emptyList()))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, testScope)
        advanceUntilIdle()

        // Act
        repository.resetAllGames()
        advanceUntilIdle()

        // Assert
        verify(mockPlayerDao).deleteAll()
        val playersCaptor = argumentCaptor<List<Player>>()
        verify(mockPlayerDao).insertAll(playersCaptor.capture())
        assertEquals(currentGameSize, playersCaptor.firstValue.size)
    }

    @Test
    fun `resetDeltaForPlayer should clear the delta and active status in gameState`() = testScope.runTest {
        // Arrange
        initializeRepositoryWithPlayers()
        advanceUntilIdle()
        repository.increaseLife(playerIndex = 0)
        advanceUntilIdle()

        // Act
        repository.resetDeltaForPlayer(playerIndex = 0)

        // Assert
        val state = repository.gameState.value
        assertEquals(0, state.playerDeltas[0])
        assertEquals(emptySet<Int>(), state.activeDeltaPlayers)
    }
}