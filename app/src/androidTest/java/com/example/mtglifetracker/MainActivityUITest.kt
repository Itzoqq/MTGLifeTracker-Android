package com.example.mtglifetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the MainActivity. This class uses Hilt for dependency injection
 * and Espresso for interacting with and verifying UI components.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityUITest {

    // HiltRule manages the Hilt components' state and is used to inject dependencies into the test.
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // ActivityScenarioRule launches the activity under test before each test method.
    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun settingsIcon_shouldBeDisplayed_onLaunch() {
        // Verifies that the settings icon is visible when the activity starts.
        onView(withId(R.id.settingsIcon)).check(matches(isDisplayed()))
    }
}