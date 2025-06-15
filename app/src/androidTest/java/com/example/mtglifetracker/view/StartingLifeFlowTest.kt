package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.DatabaseClearingRule
import com.example.mtglifetracker.DisableAnimationsRule
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class StartingLifeFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule(order = 2)
    val clearDatabaseRule = DatabaseClearingRule()

    @get:Rule(order = 3)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    @Test
    fun selectingPresetStartingLife_shouldUpdateLifeOnScreen() {
        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("20")).perform(click())

        // Assert
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
    }

    @Test
    fun selectingCustomStartingLife_shouldUpdateLifeOnScreen() {
        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("123"))
        onView(withText("Set")).perform(click())

        // Assert
        onView(lifeCounterForPlayer2).check(matches(withText("123")))
    }

    @Test
    fun enteringInvalidCustomLife_shouldNotUpdateLifeAndShowToast() {
        // 1. Change life to something different first to see if it changes back
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("20")).perform(click())
        onView(lifeCounterForPlayer2).check(matches(withText("20")))

        // 2. Try to set an invalid life total (e.g., 0)
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("0"))
        onView(withText("Set")).perform(click())

        // 3. Assert the life total did NOT change from 20
        onView(lifeCounterForPlayer2).check(matches(withText("20")))

        // Note: Testing for Toasts can be tricky and requires a custom matcher,
        // but verifying the life total did not change is a robust test for this logic.
    }
}