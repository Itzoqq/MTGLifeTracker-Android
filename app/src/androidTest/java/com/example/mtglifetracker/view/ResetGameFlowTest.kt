package com.example.mtglifetracker.view

import android.content.Context
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.data.AppDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for the complete "Reset Game" user flow.
 *
 * This class tests the user journey from the main screen, through the settings
 * and reset confirmation dialogs, and verifies that the game state is correctly
 * updated based on the user's choice.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ResetGameFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * This method runs after each test to clear all data from the database.
     * It uses ApplicationProvider to get the context and perform the database
     * operation on a background thread, avoiding the main thread.
     */
    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        db.clearAllTables()
    }

    // ... ALL OTHER TEST METHODS AND HELPER FUNCTIONS REMAIN THE SAME ...
    // A specific matcher to target the LifeCounterView for the player at angle 0.
    private val lifeCounterForPlayerAtAngleZero = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withAngle(0))
    )

    @Test
    fun resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals() {
        // Arrange: Decrease life for a specific player using a precise tap.
        onView(lifeCounterForPlayerAtAngleZero).perform(clickOnLeftHalf())
        onView(lifeCounterForPlayerAtAngleZero).check(matches(withText("39")))

        // Act: Navigate through settings to reset the current game.
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withId(R.id.rb_reset_current)).perform(click())
        onView(withText("Reset")).perform(click())

        // Assert: The life total for that player should be reset.
        onView(lifeCounterForPlayerAtAngleZero).check(matches(withText("40")))
    }

    @Test
    fun resetAllGames_shouldResetAllGameLifeTotals() {
        // Arrange: Decrease life for a specific player using a precise tap.
        onView(lifeCounterForPlayerAtAngleZero).perform(clickOnLeftHalf())
        onView(lifeCounterForPlayerAtAngleZero).check(matches(withText("39")))

        // Act: Navigate through settings to reset all games.
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withId(R.id.rb_reset_all)).perform(click())
        onView(withText("Reset")).perform(click())

        // Assert: The life total for that player should be reset.
        onView(lifeCounterForPlayerAtAngleZero).check(matches(withText("40")))
    }

    @Test
    fun cancelReset_shouldNotChangeLifeTotals() {
        // Arrange: Decrease life for a player using a precise tap.
        onView(lifeCounterForPlayerAtAngleZero).perform(clickOnLeftHalf())
        onView(lifeCounterForPlayerAtAngleZero).check(matches(withText("39")))

        // Act: Open the reset dialog and then cancel it.
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
        onView(withText("Cancel")).perform(click())

        // Assert: The life total should remain unchanged.
        onView(lifeCounterForPlayerAtAngleZero).check(matches(withText("39")))
    }

    private fun clickOnLeftHalf(): ViewAction {
        return GeneralClickAction(
            Tap.SINGLE,
            { view ->
                val screenPos = IntArray(2)
                view.getLocationOnScreen(screenPos)
                val screenX = screenPos[0] + (view.width * 0.25f) // Click at 25% of the width
                val screenY = screenPos[1] + (view.height / 2f)   // Click at the vertical center
                floatArrayOf(screenX, screenY)
            },
            Press.FINGER,
            InputDevice.SOURCE_UNKNOWN,
            MotionEvent.BUTTON_PRIMARY
        )
    }

    @Suppress("SameParameterValue")
    private fun withAngle(expectedAngle: Int): Matcher<View> {
        return object : BoundedMatcher<View, RotatableLayout>(RotatableLayout::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("with angle: $expectedAngle")
            }

            override fun matchesSafely(item: RotatableLayout): Boolean {
                return item.angle == expectedAngle
            }
        }
    }
}