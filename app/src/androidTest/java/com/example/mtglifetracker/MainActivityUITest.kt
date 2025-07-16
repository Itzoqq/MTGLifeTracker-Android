package com.example.mtglifetracker

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.view.LifeCounterView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An instrumented UI test class for verifying basic interactions on the [MainActivity].
 *
 * This class tests fundamental UI elements and user flows, such as verifying that initial
 * components are displayed, that life totals change on click, and that changing the player
 * count via the settings menu correctly updates the main screen layout. It inherits from
 * [BaseUITest] for standard test setup.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityUITest : BaseUITest() {

    /**
     * Tests that the main container and the settings icon are visible when the app launches.
     */
    @Test
    fun settingsIcon_shouldBeDisplayed_onLaunch() {
        Logger.instrumented("TEST_START: settingsIcon_shouldBeDisplayed_onLaunch")
        // Arrange & Act: The activity is launched by the BaseUITest rule.

        // Assert
        Logger.instrumented("Assert: Verifying main container and settings icon are displayed.")
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))
        onView(withId(R.id.settingsIcon)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: settingsIcon_shouldBeDisplayed_onLaunch")
    }

    /**
     * Tests that clicking on the left and right halves of a player segment correctly
     * increases and decreases the life total.
     */
    @Test
    fun lifeTotal_shouldChange_whenPlayerSegmentIsClicked() {
        Logger.instrumented("TEST_START: lifeTotal_shouldChange_whenPlayerSegmentIsClicked")
        // Arrange: The activity is launched with a default life total of 40.
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // Act & Assert: Click the left half (25% from the left edge) to increase life.
        Logger.instrumented("Act: Clicking left half of player segment to increase life.")
        onView(lifeCounterMatcher).perform(clickInXPercent(25))
        Logger.instrumented("Assert: Verifying life total is now 41.")
        onView(lifeCounterMatcher).check(matches(withText("41")))

        // Act & Assert: Click the right half (75% from the left edge) to decrease life.
        Logger.instrumented("Act: Clicking right half of player segment to decrease life.")
        onView(lifeCounterMatcher).perform(clickInXPercent(75))
        Logger.instrumented("Assert: Verifying life total is now 40.")
        onView(lifeCounterMatcher).check(matches(withText("40")))

        Logger.instrumented("TEST_PASS: lifeTotal_shouldChange_whenPlayerSegmentIsClicked")
    }

    /**
     * Tests that changing the player count via the settings menu correctly updates the
     * number of player segments visible on the main screen.
     */
    @Test
    fun playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings() {
        Logger.instrumented("TEST_START: playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings")
        // Arrange: The activity is launched.
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act: Navigate through the settings to change the player count to 4.
        Logger.instrumented("Act: Navigating to settings and changing player count to 4.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Number of Players"), click()
            ))
        onView(withText("4")).perform(click())
        pressBack()

        // Assert: Verify that there are now 4 LifeCounterView instances on the screen.
        Logger.instrumented("Assert: Verifying that there are now 4 player segments.")
        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 4))
        Logger.instrumented("TEST_PASS: playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings")
    }
}