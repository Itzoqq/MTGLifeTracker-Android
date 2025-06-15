package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsFlowTest : BaseUITest() {

    @Test
    fun clickingSettingsIcon_shouldDisplaySettingsDialog() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())

        // Assert
        onView(withText("Number of Players")).check(matches(isDisplayed()))
        onView(withText("Starting Life")).check(matches(isDisplayed()))
        onView(withText("Manage Profiles")).check(matches(isDisplayed()))
        onView(withText("Reset Game")).check(matches(isDisplayed()))
    }

    @Test
    fun openingPlayerCountDialog_shouldWorkCorrectly() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())

        // Assert
        onView(withText("2")).check(matches(isDisplayed()))
        onView(withText("6")).check(matches(isDisplayed()))
    }

    @Test
    fun openingResetGameDialog_shouldWorkCorrectly() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())

        // Assert
        onView(withText("Reset all active games")).check(matches(isDisplayed()))
        onView(withId(R.id.rb_reset_current)).check(matches(isDisplayed()))
    }

    @Test
    fun openingStartingLifeDialog_shouldWorkCorrectly() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())

        // Assert
        onView(withText("20")).check(matches(isDisplayed()))
        onView(withText("40")).check(matches(isDisplayed()))
        onView(withText("Custom")).check(matches(isDisplayed()))
    }

    @Test
    fun openingManageProfilesDialog_shouldWorkCorrectly() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())

        // Assert
        onView(withText("Manage Profiles")).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_profile)).check(matches(isDisplayed()))
    }
}