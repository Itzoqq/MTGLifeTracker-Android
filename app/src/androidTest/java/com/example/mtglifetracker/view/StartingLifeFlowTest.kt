package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class StartingLifeFlowTest : BaseUITest() {

    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    @Test
    fun selectingPresetStartingLife_shouldUpdateLifeOnScreen() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))
        onView(lifeCounterForPlayer2).check(matches(withText("40")))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("20")).perform(click())

        // Assert
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
    }

    @Test
    fun selectingCustomStartingLife_shouldUpdateLifeOnScreen() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))
        onView(lifeCounterForPlayer2).check(matches(withText("40")))

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
    fun enteringInvalidCustomLife_shouldNotUpdateLife() {
        // Arrange: Set life to a known value first
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("20")).perform(click())
        onView(lifeCounterForPlayer2).check(matches(withText("20")))

        // Act: Try to set an invalid life total (0)
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("0"))
        onView(withText("Set")).perform(click())

        // Assert: Life total should NOT have changed from 20
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
    }
}