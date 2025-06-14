package com.example.mtglifetracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mtglifetracker.data.ProfileRepository
import com.example.mtglifetracker.model.Profile
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var mockRepository: ProfileRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockRepository = mock()
        viewModel = ProfileViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addProfile_callsRepository() = runTest {
        val nickname = "New Profile"
        val color = "#FFFFFF"
        val profile = Profile(nickname = nickname, color = color)

        viewModel.addProfile(nickname, color)
        dispatcher.scheduler.runCurrent() // Execute the coroutine

        // We can't directly compare the Profile object because the ID is auto-generated.
        // So we'll have to be more creative or adjust the repository/viewmodel.
        // For now, let's assume the repository gets the correct data.
        verify(mockRepository).addProfile(profile)
    }

    @Test
    fun doesNicknameExist_callsRepository() = runTest {
        val nickname = "Existing"
        whenever(mockRepository.doesNicknameExist(nickname)).thenReturn(true)

        viewModel.doesNicknameExist(nickname)
        dispatcher.scheduler.runCurrent()

        verify(mockRepository).doesNicknameExist(nickname)
    }

    @Test
    fun deleteProfile_callsRepository() = runTest {
        val profileId = 1L
        viewModel.deleteProfile(profileId)
        dispatcher.scheduler.runCurrent()

        verify(mockRepository).deleteProfile(profileId)
    }

    @Test
    fun updateProfile_callsRepository() = runTest {
        val profile = Profile(id = 1L, nickname = "Updated", color = null)
        viewModel.updateProfile(profile)
        dispatcher.scheduler.runCurrent()

        verify(mockRepository).updateProfile(profile)
    }

    @Test
    fun getProfile_callsRepository() = runTest {
        val profileId = 1L
        viewModel.getProfile(profileId)
        dispatcher.scheduler.runCurrent()

        verify(mockRepository).getProfile(profileId)
    }

    @Test
    fun addProfile_withBlankNickname_doesNotCallRepository() = runTest {
        // Act
        viewModel.addProfile("   ", "#FFFFFF") // Blank nickname
        dispatcher.scheduler.runCurrent()

        // Assert
        // Verify that the repository's addProfile method was NEVER called
        verify(mockRepository, never()).addProfile(any())
    }
}