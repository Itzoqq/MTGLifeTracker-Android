package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.clickInXPercent
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ResetGameFlowTest : BaseUITest() {

    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    @Test
    fun resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Arrange: Change life total
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
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
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Arrange: Change life total
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
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
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Arrange: Change life total
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        // Act: Open reset dialog and cancel
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withText("Cancel")).perform(click())

        // Assert: Life total is unchanged
        onView(lifeCounterForPlayer2).check(matches(withText("39")))
    }
}