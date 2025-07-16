package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An instrumented UI test class for verifying all user flows related to setting the starting life total.
 *
 * This class tests the [StartingLifeDialogFragment] and [CustomLifeDialogFragment], ensuring that
 * both preset and custom life total selections are correctly reflected on the main game screen.
 * It inherits from [BaseUITest] for standard test setup.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class StartingLifeFlowTest : BaseUITest() {

    // A matcher for Player 2's life counter for convenience.
    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    /**
     * A helper function to navigate from the main screen to the "Starting Life" dialog.
     */
    private fun openStartingLifeDialog() {
        Logger.instrumented("Helper: Navigating to Starting Life dialog.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Starting Life"), click()
            ))
    }

    /**
     * Tests that selecting a preset life total (e.g., 20) correctly updates the life counters on the main screen.
     */
    @Test
    fun selectingPresetStartingLife_shouldUpdateLifeOnScreen() {
        Logger.instrumented("TEST_START: selectingPresetStartingLife_shouldUpdateLifeOnScreen")
        // Arrange: Verify initial life is 40.
        onView(lifeCounterForPlayer2).check(matches(withText("40")))
        Logger.instrumented("Arrange: Initial life is 40.")

        // Act: Select "20" from the starting life dialog.
        openStartingLifeDialog()
        Logger.instrumented("Act: Clicking '20' life option.")
        onView(withText("20")).perform(click())
        pressBack()

        // Assert: Verify life has updated to 20.
        Logger.instrumented("Assert: Verifying life total is now 20.")
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
        Logger.instrumented("TEST_PASS: selectingPresetStartingLife_shouldUpdateLifeOnScreen")
    }

    /**
     * Tests that entering a valid custom life total correctly updates the life counters on the main screen.
     */
    @Test
    fun selectingCustomStartingLife_shouldUpdateLifeOnScreen() {
        Logger.instrumented("TEST_START: selectingCustomStartingLife_shouldUpdateLifeOnScreen")
        // Arrange: Verify initial life is 40.
        onView(lifeCounterForPlayer2).check(matches(withText("40")))
        Logger.instrumented("Arrange: Initial life is 40.")

        // Act: Navigate to the custom life dialog and enter a valid number.
        openStartingLifeDialog()
        Logger.instrumented("Act: Navigating to custom life dialog and entering '123'.")
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("123"))
        onView(withText("Set")).perform(click())
        pressBack()

        // Assert: Verify life has updated to 123.
        Logger.instrumented("Assert: Verifying life total is now 123.")
        onView(lifeCounterForPlayer2).check(matches(withText("123")))
        Logger.instrumented("TEST_PASS: selectingCustomStartingLife_shouldUpdateLifeOnScreen")
    }

    /**
     * Tests that entering an invalid custom life total (e.g., 0) does not change the life total
     * and does not dismiss the dialog.
     */
    @Test
    fun enteringInvalidCustomLife_shouldNotUpdateLife() {
        Logger.instrumented("TEST_START: enteringInvalidCustomLife_shouldNotUpdateLife")
        // Arrange: Set a known starting life of 20.
        openStartingLifeDialog()
        onView(withText("20")).perform(click())
        pressBack()
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
        Logger.instrumented("Arrange: Life set to 20.")

        // Act: Attempt to set a custom life total of 0.
        openStartingLifeDialog()
        Logger.instrumented("Act: Navigating to custom life dialog and entering invalid value '0'.")
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("0"))
        onView(withText("Set")).perform(click())

        // Assert: The custom life dialog should still be open due to the validation failure.
        Logger.instrumented("Assert: Verifying custom life dialog is still displayed.")
        onView(withText(R.string.title_custom_starting_life)).check(matches(isDisplayed()))

        // FIX: Close the dialogs. One pressBack now dismisses all of them.
        pressBack()

        // Assert: The life total on the main screen should remain unchanged at 20.
        Logger.instrumented("Assert: Verifying life total has not changed from 20.")
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
        Logger.instrumented("TEST_PASS: enteringInvalidCustomLife_shouldNotUpdateLife")
    }

    /**
     * Tests that attempting to set an empty custom life total does not dismiss the dialog.
     */
    @Test
    fun enteringEmptyCustomLife_shouldNotDismissDialog() {
        Logger.instrumented("TEST_START: enteringEmptyCustomLife_shouldNotDismissDialog")
        // Arrange: Navigate to the custom life dialog.
        openStartingLifeDialog()
        onView(withText("Custom")).perform(click())
        Logger.instrumented("Arrange: Custom life dialog is open.")

        // Act: Ensure the input is empty and click "Set".
        Logger.instrumented("Act: Clearing text and clicking 'Set'.")
        onView(withId(R.id.et_custom_life)).perform(replaceText(""))
        onView(withText("Set")).perform(click())

        // Assert: The dialog should remain open because the empty input is not valid.
        Logger.instrumented("Assert: Verifying custom life dialog is still displayed.")
        onView(withText(R.string.title_custom_starting_life)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: enteringEmptyCustomLife_shouldNotDismissDialog")
    }
}