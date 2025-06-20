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
        dispatcher.scheduler.runCurrent()
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

    // The delta timer logic has moved to the View, so these tests are no longer applicable.
    // The simple increase/decrease tests below are sufficient.

    @Test
    fun increaseLife_shouldCallRepository() = runTest {
        val playerIndex = 0
        viewModel.increaseLife(playerIndex)
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).increaseLife(playerIndex)
    }

    @Test
    fun decreaseLife_shouldCallRepository() = runTest {
        val playerIndex = 1
        viewModel.decreaseLife(playerIndex)
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).decreaseLife(playerIndex)
    }

    @Test
    fun changeStartingLife_shouldCallRepository() = runTest {
        val newStartingLife = 20
        viewModel.changeStartingLife(newStartingLife)
        dispatcher.scheduler.runCurrent()
        verify(mockRepository).changeStartingLife(newStartingLife)
    }
}