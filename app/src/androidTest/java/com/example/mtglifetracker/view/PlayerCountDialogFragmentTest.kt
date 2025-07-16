package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.withViewCount
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An instrumented UI test class for verifying the functionality of the [PlayerCountDialogFragment].
 *
 * This class inherits from [BaseUITest], which handles boilerplate test setup.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerCountDialogFragmentTest : BaseUITest() {

    /**
     * Tests that selecting a new player count from the dialog correctly updates the main screen.
     *
     * Arrange: Verifies the initial state is a 2-player layout.
     * Act: Opens the settings menu, navigates to the player count dialog, and selects "4".
     * Assert: Verifies that the main screen is now populated with 4 player segments.
     */
    @Test
    fun selectingPlayerCount_updatesTheGameScreen() {
        Logger.instrumented("TEST_START: selectingPlayerCount_updatesTheGameScreen")

        // --- ARRANGE ---
        // Verify the initial state has 2 players.
        Logger.instrumented("Arrange: Verifying initial view count is 2.")
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 2))

        // --- ACT ---
        Logger.instrumented("Act: Clicking settings icon to open dialog.")
        onView(withId(R.id.settingsIcon)).perform(click())

        Logger.instrumented("Act: Clicking 'Number of Players' option in the settings list.")
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Number of Players"), click()
            ))

        Logger.instrumented("Act: Clicking '4' in the player count list.")
        onView(withText("4")).perform(click())

        // Close the settings dialogs to return to the main screen.
        pressBack()
        Logger.instrumented("Act: Pressing back to close dialogs.")

        // --- ASSERT ---
        // Verify that the number of player segments on screen has updated to 4.
        Logger.instrumented("Assert: Verifying the new view count is 4.")
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 4))
        Logger.instrumented("TEST_PASS: selectingPlayerCount_updatesTheGameScreen")
    }
}