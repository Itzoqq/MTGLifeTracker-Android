package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsFlowTest : BaseUITest() {

    // Helper to click on the settings icon and wait for the dialog to appear
    private fun openSettingsDialog() {
        onView(withId(R.id.settingsIcon)).perform(click())
        // A stable check to ensure the recycler view itself is present before we interact with it
        onView(withId(R.id.rv_settings_options)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingSettingsIcon_shouldDisplaySettingsDialog() {
        openSettingsDialog()
        // Check for a few items to ensure the list has been populated
        onView(withText("Number of Players")).check(matches(isDisplayed()))
        onView(withText("Reset Game")).check(matches(isDisplayed()))
    }

    @Test
    fun openingPlayerCountDialog_shouldWorkCorrectly() {
        openSettingsDialog()

        // Click the item at position 0 ("Number of Players")
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Assert
        onView(withText("2")).check(matches(isDisplayed()))
        onView(withText("6")).check(matches(isDisplayed()))
    }

    @Test
    fun openingResetGameDialog_shouldWorkCorrectly() {
        openSettingsDialog()

        // Click the item at position 3 ("Reset Game")
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(3, click()))

        // Assert
        onView(withText("Reset all active games")).check(matches(isDisplayed()))
        onView(withId(R.id.rb_reset_current)).check(matches(isDisplayed()))
    }

    @Test
    fun openingStartingLifeDialog_shouldWorkCorrectly() {
        openSettingsDialog()

        // Click the item at position 1 ("Starting Life")
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // Assert
        onView(withText("20")).check(matches(isDisplayed()))
        onView(withText("40")).check(matches(isDisplayed()))
        onView(withText("Custom")).check(matches(isDisplayed()))
    }

    @Test
    fun openingManageProfilesDialog_shouldWorkCorrectly() {
        openSettingsDialog()

        // Click the item at position 2 ("Manage Profiles")
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        // Assert
        onView(allOf(withId(R.id.tv_dialog_title), withText("Manage Profiles"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_profile)).check(matches(isDisplayed()))
    }
}