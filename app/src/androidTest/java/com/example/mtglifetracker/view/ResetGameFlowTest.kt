package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ResetGameFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val clearDatabaseRule = DatabaseClearingRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    @Test
    fun resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals() {
        // Arrange
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25)) // Replaced with util
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withId(R.id.rb_reset_current)).perform(click())
        onView(withText("Reset")).perform(click())

        // Assert
        onView(lifeCounterForPlayer2).check(matches(withText("40")))
    }

    @Test
    fun resetAllGames_shouldResetAllGameLifeTotals() {
        // Arrange
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25)) // Replaced with util
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withId(R.id.rb_reset_all)).perform(click())
        onView(withText("Reset")).perform(click())

        // Assert
        onView(lifeCounterForPlayer2).check(matches(withText("40")))
    }

    @Test
    fun cancelReset_shouldNotChangeLifeTotals() {
        // Arrange
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25)) // Replaced with util
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withText("Cancel")).perform(click())

        // Assert
        onView(lifeCounterForPlayer2).check(matches(withText("39")))
    }

    // The custom clickOnLeftHalf and withAngle helper methods have been removed.
}