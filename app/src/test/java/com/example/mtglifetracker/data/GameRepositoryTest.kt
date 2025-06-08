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
        mockPlayerDao = mock()
        mockSettingsDao = mock()
    }

    @Test
    fun `repository init should load initial data and gameState should reflect it`() = testScope.runTest {
        // Arrange
        val initialSettings = GameSettings(playerCount = 2)
        val initialPlayers = listOf(
            Player(gameSize = 2, playerIndex = 0, name = "Player 1"),
            Player(gameSize = 2, playerIndex = 1, name = "Player 2")
        )
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(initialSettings))
        whenever(mockPlayerDao.getPlayers(2)).thenReturn(flowOf(initialPlayers))

        // Act
        repository = GameRepository(mockPlayerDao, mockSettingsDao, this)

        // THIS IS THE FIX:
        // Advance the test scheduler to allow the repository's init coroutine to complete.
        advanceUntilIdle()

        // Assert
        repository.gameState.test {
            val emittedState = awaitItem()
            assertEquals(2, emittedState.playerCount)
            assertEquals(2, emittedState.players.size) // This will now pass
            assertEquals("Player 1", emittedState.players[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changePlayerCount should call saveSettings on DAO`() = testScope.runTest {
        val initialSettings = GameSettings(playerCount = 2)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(initialSettings))
        whenever(mockPlayerDao.getPlayers(2)).thenReturn(flowOf(emptyList()))
        whenever(mockPlayerDao.getPlayers(4)).thenReturn(flowOf(emptyList()))

        repository = GameRepository(mockPlayerDao, mockSettingsDao, this)
        repository.changePlayerCount(4)

        verify(mockSettingsDao).saveSettings(GameSettings(playerCount = 4))
    }

    @Test
    fun `increaseLife should update player in DAO and update gameState`() = testScope.runTest {
        // Arrange
        val initialPlayer1 = Player(gameSize = 2, playerIndex = 0, life = 40, name = "Player 1")
        val initialPlayer2 = Player(gameSize = 2, playerIndex = 1, life = 40, name = "Player 2")
        val initialSettings = GameSettings(playerCount = 2)

        // Configure the mocks to return static flows (no reactive updates)
        whenever(mockSettingsDao.getSettings()).thenReturn(flowOf(initialSettings))
        whenever(mockPlayerDao.getPlayers(2)).thenReturn(flowOf(listOf(initialPlayer1, initialPlayer2)))

        // Mock the updatePlayer method to just capture the call
        whenever(mockPlayerDao.updatePlayer(any())).thenReturn(Unit)

        // Act
        repository = GameRepository(mockPlayerDao, mockSettingsDao, this)
        advanceUntilIdle() // Let the repository initialize

        // Verify initial state
        val initialState = repository.gameState.value
        assertEquals(40, initialState.players[0].life)
        assertEquals(0, initialState.playerDeltas[0])

        repository.increaseLife(playerIndex = 0)
        advanceUntilIdle() // Let all background work complete

        // Assert
        val finalState = repository.gameState.value
        val playerCaptor = argumentCaptor<Player>()

        verify(mockPlayerDao).updatePlayer(playerCaptor.capture())
        assertEquals(41, playerCaptor.firstValue.life) // Verify the correct data was sent to the DAO

        // Debug: print the actual state to understand what's happening
        println("Final state players: ${finalState.players}")
        println("Final state deltas: ${finalState.playerDeltas}")

        // The UI state should be updated immediately by the repository
        assertEquals(41, finalState.players[0].life) // Verify the UI state is correct
        assertEquals(1, finalState.playerDeltas[0]) // This should now pass
        assertEquals(40, finalState.players[1].life) // Second player should be unchanged
        assertEquals(0, finalState.playerDeltas[1]) // Second player delta should be 0
    }
}