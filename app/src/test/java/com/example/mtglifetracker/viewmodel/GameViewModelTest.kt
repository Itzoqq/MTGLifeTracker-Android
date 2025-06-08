package com.example.mtglifetracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mtglifetracker.data.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Unit tests for the GameViewModel.
 * This class has been updated to use the latest coroutine testing APIs,
 * replacing the deprecated TestCoroutineDispatcher and runBlockingTest.
 */
@ExperimentalCoroutinesApi
class GameViewModelTest {

    // This rule swaps the background executor used by the Architecture Components with a
    // different one which executes each task synchronously.
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Use StandardTestDispatcher for more control over coroutine execution in tests.
    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: GameViewModel
    private lateinit var mockRepository: GameRepository

    @Before
    fun setup() {
        // Sets the main coroutine dispatcher to our test dispatcher. This is crucial
        // for testing ViewModels that use viewModelScope.
        Dispatchers.setMain(dispatcher)
        mockRepository = mock(GameRepository::class.java)
        viewModel = GameViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        // Resets the main dispatcher to the original one to avoid affecting other tests.
        Dispatchers.resetMain()
    }

    @Test
    fun `changePlayerCount should call repository`() = runTest {
        val newPlayerCount = 4
        viewModel.changePlayerCount(newPlayerCount)
        // We can advance the dispatcher to ensure the launched coroutine completes.
        dispatcher.scheduler.advanceUntilIdle()
        verify(mockRepository).changePlayerCount(newPlayerCount)
    }

    @Test
    fun `increaseLife should call repository`() = runTest {
        val playerIndex = 0
        viewModel.increaseLife(playerIndex)
        dispatcher.scheduler.advanceUntilIdle()
        verify(mockRepository).increaseLife(playerIndex)
    }

    @Test
    fun `decreaseLife should call repository`() = runTest {
        val playerIndex = 1
        viewModel.decreaseLife(playerIndex)
        dispatcher.scheduler.advanceUntilIdle()
        verify(mockRepository).decreaseLife(playerIndex)
    }

    @Test
    fun `resetCurrentGame should call repository`() = runTest {
        viewModel.resetCurrentGame()
        dispatcher.scheduler.advanceUntilIdle()
        verify(mockRepository).resetCurrentGame()
    }

    @Test
    fun `resetAllGames should call repository`() = runTest {
        viewModel.resetAllGames()
        dispatcher.scheduler.advanceUntilIdle()
        verify(mockRepository).resetAllGames()
    }
}