package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An instrumented UI test class for verifying the main settings navigation flows.
 *
 * This class ensures that clicking on each item in the main settings menu
 * correctly opens the corresponding sub-dialog fragment. It inherits from
 * [BaseUITest] for standard test setup.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsFlowTest : BaseUITest() {

    /**
     * A helper function to encapsulate the actions required to open the main settings dialog.
     */
    private fun openSettingsDialog() {
        Logger.instrumented("Helper: Opening main settings dialog.")
        onView(withId(R.id.settingsIcon)).perform(click())
        // A stable check to ensure the dialog's title is present before we interact with its items.
        onView(withText("Settings")).check(matches(isDisplayed()))
    }

    /**
     * Tests that clicking the main settings icon correctly displays the settings dialog
     * with all the expected options.
     */
    @Test
    fun clickingSettingsIcon_shouldDisplaySettingsDialog() {
        Logger.instrumented("TEST_START: clickingSettingsIcon_shouldDisplaySettingsDialog")
        // Act
        Logger.instrumented("Act: Opening settings dialog.")
        openSettingsDialog()

        // Assert
        Logger.instrumented("Assert: Verifying dialog options are displayed.")
        onView(withText("Number of Players")).check(matches(isDisplayed()))
        onView(withText("Reset Game")).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: clickingSettingsIcon_shouldDisplaySettingsDialog")
    }

    /**
     * Tests that clicking the "Number of Players" option opens the correct sub-dialog.
     */
    @Test
    fun openingPlayerCountDialog_shouldWorkCorrectly() {
        Logger.instrumented("TEST_START: openingPlayerCountDialog_shouldWorkCorrectly")
        // Arrange
        openSettingsDialog()

        // Act
        Logger.instrumented("Act: Clicking 'Number of Players' option.")
        onView(withText("Number of Players")).perform(click())

        // Assert
        Logger.instrumented("Assert: Verifying player count dialog is displayed.")
        onView(withText("2")).check(matches(isDisplayed()))
        onView(withText("6")).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: openingPlayerCountDialog_shouldWorkCorrectly")
    }

    /**
     * Tests that clicking the "Reset Game" option opens the correct sub-dialog.
     */
    @Test
    fun openingResetGameDialog_shouldWorkCorrectly() {
        Logger.instrumented("TEST_START: openingResetGameDialog_shouldWorkCorrectly")
        // Arrange
        openSettingsDialog()

        // Act
        Logger.instrumented("Act: Clicking 'Reset Game' option.")
        onView(withText("Reset Game")).perform(click())

        // Assert
        Logger.instrumented("Assert: Verifying reset game dialog is displayed.")
        onView(withText("Reset all active games")).check(matches(isDisplayed()))
        onView(withId(R.id.rb_reset_current)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: openingResetGameDialog_shouldWorkCorrectly")
    }

    /**
     * Tests that clicking the "Starting Life" option opens the correct sub-dialog.
     */
    @Test
    fun openingStartingLifeDialog_shouldWorkCorrectly() {
        Logger.instrumented("TEST_START: openingStartingLifeDialog_shouldWorkCorrectly")
        // Arrange
        openSettingsDialog()

        // Act
        Logger.instrumented("Act: Clicking 'Starting Life' option.")
        // **THE FIX**: Use a direct click on the text.
        onView(withText("Starting Life")).perform(click())

        // Assert
        Logger.instrumented("Assert: Verifying starting life dialog is displayed.")
        onView(withText("20")).check(matches(isDisplayed()))
        onView(withText("40")).check(matches(isDisplayed()))
        onView(withText("Custom")).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: openingStartingLifeDialog_shouldWorkCorrectly")
    }

    /**
     * Tests that clicking the "Manage Profiles" option opens the correct sub-dialog.
     */
    @Test
    fun openingManageProfilesDialog_shouldWorkCorrectly() {
        Logger.instrumented("TEST_START: openingManageProfilesDialog_shouldWorkCorrectly")
        // Arrange
        openSettingsDialog()

        // Act
        Logger.instrumented("Act: Clicking 'Manage Profiles' option.")
        onView(withText("Manage Profiles")).perform(click())

        // Assert
        Logger.instrumented("Assert: Verifying manage profiles dialog is displayed.")
        onView(allOf(withId(R.id.tv_dialog_title), withText("Manage Profiles"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_profile)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: openingManageProfilesDialog_shouldWorkCorrectly")
    }
}