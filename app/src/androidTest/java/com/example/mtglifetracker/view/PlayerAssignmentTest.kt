package com.example.mtglifetracker.view

import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.directlyPerformClick
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.withViewCount
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

// Helper function to assert the background color of a view.
fun withBackgroundColor(colorResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("with background color from resource id: $colorResId")
        }

        override fun matchesSafely(view: View): Boolean {
            val context = view.context
            val expectedColor = ContextCompat.getColor(context, colorResId)
            val background = view.background
            if (background is ColorDrawable) {
                return background.color == expectedColor
            }
            return false
        }
    }
}

/**
 * A robust polling function that waits for a view to appear on screen.
 * This should be called directly from the test thread.
 */
fun waitForView(viewMatcher: Matcher<View>, timeoutMillis: Long = 5000, checkIntervalMillis: Long = 100) {
    val endTime = System.currentTimeMillis() + timeoutMillis
    do {
        try {
            // THE FIX: Check that at least 1% of the view is displayed.
            // This guarantees the view has been laid out with a width and height.
            onView(viewMatcher).check(matches(isDisplayingAtLeast(1)))
            return
        } catch (_: AssertionFailedError) {
            Thread.sleep(checkIntervalMillis)
        } catch (_: NoMatchingViewException) {
            Thread.sleep(checkIntervalMillis)
        }
    } while (System.currentTimeMillis() < endTime)

    throw TimeoutException("View with matcher '$viewMatcher' did not appear within $timeoutMillis ms")
}

/**
 * An instrumented UI test class for verifying all user flows related to player profile
 * assignment and management.
 *
 * This class inherits from [BaseUITest], which handles the boilerplate setup for Hilt,
 * activity launching, and database clearing. Each test simulates a specific user scenario,
 * such as assigning a profile, attempting to assign a used profile, deleting an
 * assigned profile, and ensuring state is preserved across configuration changes.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerAssignmentTest : BaseUITest() {

    /**
     * A helper function to create a test profile with a given nickname and optional color.
     * This encapsulates the repeatable UI actions for profile creation.
     */
    private fun createTestProfile(nickname: String, color: String? = null) {
        Logger.instrumented("Helper: Creating profile '$nickname' with color '$color'.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    withText("Manage Profiles"), click()
                ))

        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText(nickname))

        if (color != null) {
            val colorOptions = listOf(
                "#F44336", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800"
            )
            val colorIndex = colorOptions.indexOf(color)
            if (colorIndex != -1) {
                onView(withId(R.id.rv_colors)).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(colorIndex, click())
                )
            } else {
                throw IllegalArgumentException("Color $color not found in test options.")
            }
        }

        onView(withText("Save")).perform(click())
        pressBack()
        Logger.instrumented("Helper: Profile '$nickname' created successfully.")
    }

    /**
     * Tests the fundamental flow of assigning a profile to a player and then unloading it.
     * Arrange: Creates a test profile.
     * Act: Clicks the player name, selects the profile from the popup.
     * Assert: Verifies the player name and background color are updated.
     * Act: Long-presses the player name to unload the profile.
     * Assert: Verifies the player name and background color revert to their default states.
     */
    @Test
    fun assignAndUnloadProfile_onNonRotatedSegment_isSuccessful() {
        Logger.instrumented("TEST_START: assignAndUnloadProfile_onNonRotatedSegment_isSuccessful")
        // --- ARRANGE ---
        val profileName = "TestProfile"
        createTestProfile(profileName, "#4CAF50")
        val nonRotatedSegmentMatcher = withTagValue(equalTo("player_segment_1"))
        val playerNameMatcher = allOf(withId(R.id.tv_player_name), isDescendantOfA(nonRotatedSegmentMatcher))
        Logger.instrumented("Arrange: Profile and view matchers created.")

        // --- ACT & ASSERT (ASSIGN) ---
        Logger.instrumented("Act: Clicking player name and selecting profile.")
        onView(playerNameMatcher).perform(directlyPerformClick())
        val profilePopupRecyclerMatcher = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(nonRotatedSegmentMatcher))
        onView(profilePopupRecyclerMatcher)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))
        waitForView(allOf(playerNameMatcher, withText(profileName)))
        Logger.instrumented("Assert: Verifying profile was assigned correctly.")
        onView(playerNameMatcher).check(matches(withText(profileName)))
        onView(nonRotatedSegmentMatcher).check(matches(withBackgroundColor(R.color.delta_positive)))

        // --- ACT & ASSERT (UNLOAD) ---
        Logger.instrumented("Act: Long-clicking player name to unload profile.")
        onView(playerNameMatcher).perform(longClick())
        waitForView(allOf(playerNameMatcher, withText("Player 2")))
        Logger.instrumented("Assert: Verifying profile was unloaded and view reverted to default.")
        onView(playerNameMatcher).check(matches(withText("Player 2")))
        onView(nonRotatedSegmentMatcher).check(matches(withBackgroundColor(R.color.default_segment_background)))
        Logger.instrumented("TEST_PASS: assignAndUnloadProfile_onNonRotatedSegment_isSuccessful")
    }

    /**
     * Tests that a profile already assigned to one player cannot be assigned to another.
     * Arrange: Creates two profiles and assigns the first one to Player 1.
     * Act: Opens the profile selection popup for Player 2.
     * Assert: Verifies that the second profile is visible in the list, but the first (assigned) one is not.
     */
    @Test
    fun assigningUsedProfile_isNotPossible() {
        Logger.instrumented("TEST_START: assigningUsedProfile_isNotPossible")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Creating two profiles and assigning 'PlayerOne' to segment 0.")
        createTestProfile("PlayerOne")
        createTestProfile("PlayerTwo")
        val segment0playerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        onView(segment0playerName).perform(directlyPerformClick())
        onView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("PlayerOne")), directlyPerformClick()
            ))
        waitForView(allOf(segment0playerName, withText("PlayerOne")))

        // --- ACT ---
        Logger.instrumented("Act: Opening profile selection for segment 1.")
        val segment1playerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_1"))))
        onView(segment1playerName).perform(directlyPerformClick())

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying 'PlayerTwo' is visible and 'PlayerOne' does not exist in the popup.")
        val segment1popupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_1"))))
        onView(allOf(withText("PlayerTwo"), isDescendantOfA(segment1popupRecycler))).check(matches(isDisplayed()))
        onView(allOf(withText("PlayerOne"), isDescendantOfA(segment1popupRecycler))).check(doesNotExist())
        Logger.instrumented("TEST_PASS: assigningUsedProfile_isNotPossible")
    }

    /**
     * Tests that attempting to open the profile popup for a player shows a Snackbar if no unassigned profiles are available.
     * Arrange: Creates a single profile and assigns it to Player 1.
     * Act: Clicks on Player 2's name.
     * Assert: Verifies that a Snackbar with the correct message appears and the profile popup does not open.
     */
    @Test
    fun openingProfilePopup_withNoAvailableProfiles_showsSnackbar() {
        Logger.instrumented("TEST_START: openingProfilePopup_withNoAvailableProfiles_showsSnackbar")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Creating one profile and assigning it to segment 0.")
        createTestProfile("TheOnlyProfile")
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        onView(segment0PlayerName).perform(directlyPerformClick())
        onView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("TheOnlyProfile")), directlyPerformClick()
            ))
        waitForView(allOf(segment0PlayerName, withText("TheOnlyProfile")))

        // --- ACT ---
        Logger.instrumented("Act: Clicking on player name for segment 1.")
        val segment1PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_1"))))
        onView(segment1PlayerName).perform(directlyPerformClick())

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying Snackbar is displayed and popup is not.")
        onView(withText("No other profiles available")).check(matches(isDisplayed()))
        val segment1PopupContainer = allOf(withId(R.id.profile_popup_container), isDescendantOfA(withTagValue(equalTo("player_segment_1"))))
        onView(segment1PopupContainer).check(matches(not(isDisplayed())))
        Logger.instrumented("TEST_PASS: openingProfilePopup_withNoAvailableProfiles_showsSnackbar")
    }

    /**
     * Tests that if a profile assigned to a player is deleted from the settings, the player's segment correctly reverts to its default state.
     * Arrange: Creates a profile and assigns it to Player 1.
     * Act: Navigates to settings and deletes the profile.
     * Assert: Verifies that Player 1's name and background color revert to default on the main screen.
     */
    @Test
    fun deletingAssignedProfile_revertsPlayerSegmentToDefault() {
        Logger.instrumented("TEST_START: deletingAssignedProfile_revertsPlayerSegmentToDefault")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Creating and assigning profile 'ToDelete'.")
        val profileName = "ToDelete"
        createTestProfile(profileName, "#9C27B0")
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        onView(segment0PlayerName).perform(directlyPerformClick())
        onView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))
        waitForView(allOf(segment0PlayerName, withText(profileName)))

        // --- ACT ---
        Logger.instrumented("Act: Navigating to settings and deleting the profile.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Manage Profiles"), click()
            ))
        onView(withText(profileName)).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("DELETE")).perform(click())
        pressBack()

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying player segment reverts to default.")
        waitForView(allOf(segment0PlayerName, withText("Player 1")))
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        onView(withTagValue(equalTo("player_segment_0"))).check(matches(withBackgroundColor(R.color.default_segment_background)))
        Logger.instrumented("TEST_PASS: deletingAssignedProfile_revertsPlayerSegmentToDefault")
    }

    /**
     * Tests that profile assignments are correctly preserved when switching the player count away and then back.
     * Arrange: Sets up a 4-player game and assigns a profile to Player 3.
     * Act: Switches to a 2-player game.
     * Assert: Verifies the profile is gone.
     * Act: Switches back to a 4-player game.
     * Assert: Verifies the original profile assignment to Player 3 is restored.
     */
    @Test
    fun profileAssignmentsArePreservedWhenSwitchingPlayerCount() {
        Logger.instrumented("TEST_START: profileAssignmentsArePreservedWhenSwitchingPlayerCount")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Setting up 4-player game and assigning profile to segment 2.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())
        pressBack()
        waitForView(withTagValue(equalTo("player_segment_3")))
        val profileName = "TestProfile"
        createTestProfile(profileName)
        val segment2PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_2"))))
        onView(segment2PlayerName).perform(directlyPerformClick())
        waitForView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_2")))))
        onView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_2")))))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))
        waitForView(allOf(segment2PlayerName, withText(profileName)))

        // --- ACT 1 & ASSERT 1 ---
        Logger.instrumented("Act: Switching to 2-player game.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("2")).perform(click())
        pressBack()
        waitForView(withTagValue(equalTo("player_segment_1")))
        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 2))
        onView(segment2PlayerName).check(doesNotExist())
        Logger.instrumented("Assert: Verified 2-player game state is correct.")

        // --- ACT 2 & ASSERT 2 ---
        Logger.instrumented("Act: Switching back to 4-player game.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())
        pressBack()
        Logger.instrumented("Assert: Verifying original profile assignment was restored.")
        waitForView(allOf(segment2PlayerName, withText(profileName)))
        onView(segment2PlayerName).check(matches(withText(profileName)))
        Logger.instrumented("TEST_PASS: profileAssignmentsArePreservedWhenSwitchingPlayerCount")
    }

    /**
     * Tests that resetting the current game clears all profile assignments for that game size.
     * Arrange: Creates a profile and assigns it to Player 1.
     * Act: Navigates to settings and resets the current game.
     * Assert: Verifies Player 1's segment has reverted to its default state.
     */
    @Test
    fun resettingCurrentGame_clearsProfileAssignments() {
        Logger.instrumented("TEST_START: resettingCurrentGame_clearsProfileAssignments")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Creating and assigning a profile.")
        createTestProfile("ProfileToReset", "#FF9800")
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        onView(segment0PlayerName).perform(directlyPerformClick())
        waitForView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
        onView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("ProfileToReset")), directlyPerformClick()
            ))
        waitForView(allOf(segment0PlayerName, withText("ProfileToReset")))

        // --- ACT ---
        Logger.instrumented("Act: Resetting the current game.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Reset Game"), click()
            ))
        onView(withText("Reset")).perform(click())
        pressBack()

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying player segment has reverted to default.")
        waitForView(allOf(segment0PlayerName, withText("Player 1")))
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        onView(withTagValue(equalTo("player_segment_0"))).check(matches(withBackgroundColor(R.color.default_segment_background)))
        Logger.instrumented("TEST_PASS: resettingCurrentGame_clearsProfileAssignments")
    }

    /**
     * Tests that changing the starting life total clears all profile assignments.
     * Arrange: Creates and assigns a profile.
     * Act: Navigates to settings and changes the starting life total.
     * Assert: Verifies the assigned player segment reverts to its default state.
     */
    @Test
    fun changingStartingLife_clearsProfileAssignments() {
        Logger.instrumented("TEST_START: changingStartingLife_clearsProfileAssignments")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Creating and assigning a profile.")
        createTestProfile("TestProfile", "#4CAF50")
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        onView(segment0PlayerName).perform(directlyPerformClick())
        waitForView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
        onView(allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0")))))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("TestProfile")), directlyPerformClick()
            ))
        waitForView(allOf(segment0PlayerName, withText("TestProfile")))

        // --- ACT ---
        Logger.instrumented("Act: Changing the starting life total.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("20")).perform(click())
        pressBack()

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying player segment has reverted to default.")
        waitForView(allOf(segment0PlayerName, withText("Player 1")))
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        onView(withTagValue(equalTo("player_segment_0"))).check(matches(withBackgroundColor(R.color.default_segment_background)))
        Logger.instrumented("TEST_PASS: changingStartingLife_clearsProfileAssignments")
    }

    /**
     * Tests that a user cannot assign a profile from a stale UI state.
     * Arrange: Creates one profile. Opens the profile popup for both Player 1 and Player 2.
     * Act: Player 2 selects the profile first. Then, Player 1 (with the now stale popup) tries to select the same profile.
     * Assert: Verifies that a Snackbar appears for Player 1 and their name does not change.
     */
    @Test
    fun selectingStaleProfile_whenAlreadyAssignedByOtherPlayer_showsSnackbar() {
        Logger.instrumented("TEST_START: selectingStaleProfile_whenAlreadyAssignedByOtherPlayer_showsSnackbar")
        // --- ARRANGE ---
        Logger.instrumented("Arrange: Creating one profile and opening popups for both players.")
        val profileName = "TestProfile"
        createTestProfile(profileName)
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        val segment1PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(withTagValue(equalTo("player_segment_1"))))
        onView(segment0PlayerName).perform(directlyPerformClick())
        onView(segment1PlayerName).perform(directlyPerformClick())
        val segment0PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_0"))))
        val segment1PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(withTagValue(equalTo("player_segment_1"))))
        waitForView(allOf(withText(profileName), isDescendantOfA(segment0PopupRecycler)))
        waitForView(allOf(withText(profileName), isDescendantOfA(segment1PopupRecycler)))

        // --- ACT ---
        Logger.instrumented("Act: Player 2 selects profile first, then Player 1 attempts to select the same one.")
        // Player 2 selects the profile.
        onView(segment1PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))
        waitForView(allOf(segment1PlayerName, withText(profileName)))
        // Player 1, with the stale popup, now tries to select it.
        onView(segment0PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        // --- ASSERT ---
        Logger.instrumented("Assert: Verifying Snackbar appears and Player 1's name is unchanged.")
        onView(withText("'$profileName' is already in use.")).check(matches(isDisplayed()))
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        Logger.instrumented("TEST_PASS: selectingStaleProfile_whenAlreadyAssignedByOtherPlayer_showsSnackbar")
    }
}