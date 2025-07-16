package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.clickInXPercent
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An instrumented UI test class for verifying the functionality of the game reset flows.
 *
 * This class tests the [ResetConfirmationDialogFragment], ensuring that both the "Reset Current Game"
 * and "Reset All Games" options correctly reset the player life totals on the main screen.
 * It inherits from [BaseUITest] for standard test setup.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ResetGameFlowTest : BaseUITest() {

    // A matcher for Player 2's life counter for convenience.
    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    /**
     * A helper function to navigate from the main screen to the "Reset Game" dialog.
     */
    private fun openResetDialog() {
        Logger.instrumented("Helper: Navigating to Reset Game dialog.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Reset Game")).perform(click())
    }

    /**
     * Tests that the "Reset Current Game" option resets the life total for the current game only.
     * Arrange: Changes a player's life total away from the default.
     * Act: Opens the reset dialog, selects "Reset current game", and confirms.
     * Assert: Verifies that the player's life total has returned to the default of 40.
     */
    @Test
    fun resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals() {
        Logger.instrumented("TEST_START: resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals")
        // --- ARRANGE ---
        // Change a player's life total so we can see the effect of the reset.
        // ** THE FIX: Click at 25% (left half) to DECREMENT life **
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
        onView(lifeCounterForPlayer2).check(matches(withText("39")))
        Logger.instrumented("Arrange: Player 2 life set to 39.")

        // --- ACT ---
        openResetDialog()
        Logger.instrumented("Act: Clicking 'Reset current' radio button and confirming.")
        onView(withId(R.id.rb_reset_current)).perform(click())
        onView(withText("Reset")).perform(click())
        pressBack()

        // --- ASSERT ---
        // Verifies that the life total has been reset to the default value.
        Logger.instrumented("Assert: Verifying player life has been reset to 40.")
        onView(lifeCounterForPlayer2).check(matches(withText("40")))
        Logger.instrumented("TEST_PASS: resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals")
    }

    /**
     * Tests that the "Reset All Games" option resets the life total.
     * Arrange: Changes a player's life total away from the default.
     * Act: Opens the reset dialog, selects "Reset all games", and confirms.
     * Assert: Verifies that the player's life total has returned to the default of 40.
     */
    @Test
    fun resetAllGames_shouldResetAllGameLifeTotals() {
        Logger.instrumented("TEST_START: resetAllGames_shouldResetAllGameLifeTotals")
        // --- ARRANGE ---
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25)) // Use 25% to decrement
        onView(lifeCounterForPlayer2).check(matches(withText("39")))
        Logger.instrumented("Arrange: Player 2 life set to 39.")

        // --- ACT ---
        openResetDialog()
        Logger.instrumented("Act: Clicking 'Reset all' radio button and confirming.")
        onView(withId(R.id.rb_reset_all)).perform(click())
        onView(withText("Reset")).perform(click())
        pressBack()

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying player life has been reset to 40.")
        onView(lifeCounterForPlayer2).check(matches(withText("40")))
        Logger.instrumented("TEST_PASS: resetAllGames_shouldResetAllGameLifeTotals")
    }

    /**
     * Tests that canceling the reset operation leaves the life totals unchanged.
     * Arrange: Changes a player's life total away from the default.
     * Act: Opens the reset dialog and clicks "Cancel".
     * Assert: Verifies that the life total remains unchanged.
     */
    @Test
    fun cancelReset_shouldNotChangeLifeTotals() {
        Logger.instrumented("TEST_START: cancelReset_shouldNotChangeLifeTotals")
        // --- ARRANGE ---
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25)) // Use 25% to decrement
        onView(lifeCounterForPlayer2).check(matches(withText("39")))
        Logger.instrumented("Arrange: Player 2 life set to 39.")

        // --- ACT ---
        openResetDialog()
        Logger.instrumented("Act: Clicking 'Cancel' button in reset dialog.")
        onView(withText("Cancel")).perform(click())

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying player life has not changed.")
        onView(lifeCounterForPlayer2).check(matches(withText("39")))
        Logger.instrumented("TEST_PASS: cancelReset_shouldNotChangeLifeTotals")
    }
}