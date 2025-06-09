package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun clickingSettingsIcon_shouldDisplaySettingsDialog() {
        // Act
        onView(withId(R.id.settingsIcon)).perform(click())

        // Assert
        onView(withText("Number of Players")).check(matches(isDisplayed()))
        onView(withText("Reset Game")).check(matches(isDisplayed()))
    }

    @Test
    fun openingPlayerCountDialog_shouldWorkCorrectly() {
        // Arrange: Open the main settings dialog
        onView(withId(R.id.settingsIcon)).perform(click())

        // Act: Click on the "Number of Players" option
        onView(withText("Number of Players")).perform(click())

        // Assert: Check that the player count dialog is now displayed with its options
        onView(withText("2")).check(matches(isDisplayed()))
        onView(withText("6")).check(matches(isDisplayed()))
    }

    @Test
    fun openingResetGameDialog_shouldWorkCorrectly() {
        // Arrange: Open the main settings dialog
        onView(withId(R.id.settingsIcon)).perform(click())

        // Act: Click on the "Reset Game" option
        onView(withText("Reset Game")).perform(click())

        // Assert: Check that the reset dialog is displayed with its options
        onView(withText("Reset all active games")).check(matches(isDisplayed()))
        onView(withId(R.id.rb_reset_current)).check(matches(isDisplayed()))
    }
}
