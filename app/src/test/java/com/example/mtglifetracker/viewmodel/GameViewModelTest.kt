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
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
class GameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: GameViewModel
    private lateinit var mockRepository: GameRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockRepository = mock(GameRepository::class.java)
        viewModel = GameViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun changePlayerCount_shouldCallRepository() = runTest {
        val newPlayerCount = 4
        viewModel.changePlayerCount(newPlayerCount)
        dispatcher.scheduler.runCurrent() // Use runCurrent for consistency
        verify(mockRepository).changePlayerCount(newPlayerCount)
    }

    @Test
    fun resetCurrentGame_shouldCallRepository() = runTest {
        viewModel.resetCurrentGame()
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).resetCurrentGame()
    }

    @Test
    fun resetAllGames_shouldCallRepository() = runTest {
        viewModel.resetAllGames()
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).resetAllGames()
    }

    @Test
    fun increaseLife_shouldCallRepositoryAndStartResetTimer() = runTest {
        val playerIndex = 0
        viewModel.increaseLife(playerIndex)

        // Run tasks scheduled for the current time
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).increaseLife(playerIndex)

        // The timer is scheduled, but the delay hasn't passed,
        // so resetDeltaForPlayer should not have been called.
        verify(mockRepository, never()).resetDeltaForPlayer(playerIndex)

        // Advance time by 3000ms to trigger the timer
        dispatcher.scheduler.advanceTimeBy(3000)
        dispatcher.scheduler.runCurrent() // Run the newly ready task

        // Now, verify the reset function has been called
        verify(mockRepository).resetDeltaForPlayer(playerIndex)
    }

    @Test
    fun decreaseLife_shouldCallRepositoryAndStartResetTimer() = runTest {
        val playerIndex = 1
        viewModel.decreaseLife(playerIndex)

        // Run tasks scheduled for the current time
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).decreaseLife(playerIndex)

        // Verify the reset function has NOT been called yet
        verify(mockRepository, never()).resetDeltaForPlayer(playerIndex)

        // Advance time by 3000ms to trigger the timer
        dispatcher.scheduler.advanceTimeBy(3000)
        dispatcher.scheduler.runCurrent() // Run the newly ready task

        // Now, verify the reset function has been called
        verify(mockRepository).resetDeltaForPlayer(playerIndex)
    }

    @Test
    fun multipleLifeChanges_shouldRestartTheResetTimer() = runTest {
        val playerIndex = 0
        // First life change
        viewModel.increaseLife(playerIndex)
        dispatcher.scheduler.runCurrent()

        // Advance time part-way
        dispatcher.scheduler.advanceTimeBy(1500)

        // Second life change, which should reset the timer
        viewModel.increaseLife(playerIndex)
        dispatcher.scheduler.runCurrent()

        // Verify increaseLife was called twice
        verify(mockRepository, times(2)).increaseLife(playerIndex)

        // Advance time by another 2999ms. The total time since the *second*
        // call is 2999ms, so the timer should not have fired yet.
        dispatcher.scheduler.advanceTimeBy(2999)
        dispatcher.scheduler.runCurrent()
        verify(mockRepository, never()).resetDeltaForPlayer(playerIndex)

        // Advance time by 1ms more. Now 3000ms have passed.
        dispatcher.scheduler.advanceTimeBy(1)
        dispatcher.scheduler.runCurrent()

        // Verify the reset function was finally called once.
        verify(mockRepository).resetDeltaForPlayer(playerIndex)
    }
}