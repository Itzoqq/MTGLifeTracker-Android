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


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerAssignmentTest : BaseUITest() {

    private fun createTestProfile(nickname: String, color: String? = null) {
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
    }

    @Test
    fun assignAndUnloadProfile_onNonRotatedSegment_isSuccessful() {
        // --- ARRANGE ---
        val profileName = "TestProfile"
        createTestProfile(profileName, "#4CAF50")

        val nonRotatedSegmentMatcher = withTagValue(equalTo("player_segment_1"))
        val playerNameMatcher = allOf(withId(R.id.tv_player_name), isDescendantOfA(nonRotatedSegmentMatcher))

        // --- ACT & ASSERT (ASSIGN) ---
        onView(playerNameMatcher).perform(directlyPerformClick())
        val profilePopupRecyclerMatcher = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(nonRotatedSegmentMatcher))
        onView(profilePopupRecyclerMatcher)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        waitForView(allOf(playerNameMatcher, withText(profileName)))

        onView(playerNameMatcher).check(matches(withText(profileName)))
        onView(nonRotatedSegmentMatcher).check(matches(withBackgroundColor(R.color.delta_positive)))

        // --- ACT & ASSERT (UNLOAD) ---
        onView(playerNameMatcher).perform(longClick())

        // Use the new waitForView function to wait for the UI to update
        waitForView(allOf(playerNameMatcher, withText("Player 2")))

        onView(playerNameMatcher).check(matches(withText("Player 2")))
        onView(nonRotatedSegmentMatcher).check(matches(withBackgroundColor(R.color.default_segment_background)))
    }

    @Test
    fun assigningUsedProfile_isNotPossible() {
        // --- ARRANGE ---
        // 1. Create two separate profiles we can test with.
        createTestProfile("PlayerOne")
        createTestProfile("PlayerTwo")

        // 2. Define matchers for both player segments to keep things clear.
        val segment0matcher = withTagValue(equalTo("player_segment_0"))
        val segment0playerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment0matcher))
        val segment0popupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment0matcher))

        val segment1matcher = withTagValue(equalTo("player_segment_1"))
        val segment1playerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment1matcher))
        val segment1popupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment1matcher))

        // 3. Assign "PlayerOne" to the first player segment.
        onView(segment0playerName).perform(directlyPerformClick())
        onView(segment0popupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("PlayerOne")), directlyPerformClick()
            ))

        // 4. Wait to confirm the assignment was successful before proceeding.
        waitForView(allOf(segment0playerName, withText("PlayerOne")))

        // --- ACT ---
        // 5. Open the profile selection popup for the SECOND player.
        onView(segment1playerName).perform(directlyPerformClick())

        // --- ASSERT ---
        // 6. Check the contents of the second player's popup.
        //    "PlayerTwo" should be visible and available to select.
        onView(allOf(withText("PlayerTwo"), isDescendantOfA(segment1popupRecycler)))
            .check(matches(isDisplayed()))

        // 7. "PlayerOne" should NOT exist in this list, as it's already in use.
        //    The `doesNotExist()` assertion is perfect for this check.
        onView(allOf(withText("PlayerOne"), isDescendantOfA(segment1popupRecycler)))
            .check(doesNotExist())
    }

    @Test
    fun openingProfilePopup_withNoAvailableProfiles_showsSnackbar() { // Renamed for clarity
        // --- ARRANGE ---
        createTestProfile("TheOnlyProfile")

        val segment0Matcher = withTagValue(equalTo("player_segment_0"))
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment0Matcher))
        val segment0PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment0Matcher))

        onView(segment0PlayerName).perform(directlyPerformClick())
        onView(segment0PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("TheOnlyProfile")), directlyPerformClick()
            ))
        waitForView(allOf(segment0PlayerName, withText("TheOnlyProfile")))

        val segment1Matcher = withTagValue(equalTo("player_segment_1"))
        val segment1PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment1Matcher))

        // --- ACT ---
        onView(segment1PlayerName).perform(directlyPerformClick())

        // --- ASSERT ---
        // Assert that the Snackbar is displayed with the correct text. It's that simple!
        onView(withText("No other profiles available"))
            .check(matches(isDisplayed()))

        // As a final check, ensure the profile popup for player 2 did NOT open.
        val segment1PopupContainer = allOf(withId(R.id.profile_popup_container), isDescendantOfA(segment1Matcher))
        onView(segment1PopupContainer).check(matches(not(isDisplayed())))
    }

    @Test
    fun deletingAssignedProfile_revertsPlayerSegmentToDefault() {
        // --- ARRANGE ---
        // 1. Create a profile with a color and assign it to Player 1.
        val profileName = "ToDelete"
        createTestProfile(profileName, "#9C27B0") // A purple color

        val segment0Matcher = withTagValue(equalTo("player_segment_0"))
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment0Matcher))
        val segment0PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment0Matcher))

        onView(segment0PlayerName).perform(directlyPerformClick())
        onView(segment0PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        // 2. Verify the profile was assigned correctly.
        waitForView(allOf(segment0PlayerName, withText(profileName)))
        onView(segment0PlayerName).check(matches(withText(profileName)))

        // --- ACT ---
        // 3. Navigate to the Manage Profiles dialog.
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Manage Profiles"), click()
            ))

        // 4. Delete the profile. This involves a long click to bring up the edit/delete menu.
        onView(withText(profileName)).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("DELETE")).perform(click())

        // 5. Return to the main screen.
        pressBack() // This should dismiss all dialogs.

        // --- ASSERT ---
        // 6. Wait for the player name to revert to its default state.
        //    This is crucial because the update happens via a background Flow.
        waitForView(allOf(segment0PlayerName, withText("Player 1")))

        // 7. Verify the player segment has fully reverted to its default state.
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        onView(segment0Matcher).check(matches(withBackgroundColor(R.color.default_segment_background)))
    }

    @Test
    fun profileAssignmentsArePreservedWhenSwitchingPlayerCount() {
        // --- ARRANGE: Set up a 4-player game with a custom profile ---
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())
        pressBack()

        waitForView(withTagValue(equalTo("player_segment_3")))
        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 4))

        val profileName = "TestProfile"
        createTestProfile(profileName)

        val segment2Matcher = withTagValue(equalTo("player_segment_2"))
        val segment2PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment2Matcher))
        val segment2PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment2Matcher))

        onView(segment2PlayerName).perform(directlyPerformClick())

        // Wait for the popup's RecyclerView to be fully laid out before clicking
        waitForView(segment2PopupRecycler)

        onView(segment2PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        waitForView(allOf(segment2PlayerName, withText(profileName)))
        onView(segment2PlayerName).check(matches(withText(profileName)))

        // --- ACT: Switch to a 2-player game ---
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("2")).perform(click())
        pressBack()

        // --- ASSERT: Verify 2-player game is in its default state ---
        waitForView(withTagValue(equalTo("player_segment_1")))
        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 2))
        onView(segment2PlayerName).check(doesNotExist())

        // --- ACT: Switch back to the 4-player game ---
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())
        pressBack()

        // --- FINAL ASSERT: Verify the original profile assignment was restored ---
        waitForView(allOf(segment2PlayerName, withText(profileName)))
        onView(segment2PlayerName).check(matches(withText(profileName)))
    }

    @Test
    fun resettingCurrentGame_clearsProfileAssignments() {
        // --- ARRANGE ---
        // 1. Create a profile and assign it to Player 1.
        val profileName = "ProfileToReset"
        createTestProfile(profileName, "#FF9800") // An orange color

        val segment0Matcher = withTagValue(equalTo("player_segment_0"))
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment0Matcher))
        val segment0PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment0Matcher))

        onView(segment0PlayerName).perform(directlyPerformClick())
        waitForView(segment0PopupRecycler) // Wait for the popup to be laid out
        onView(segment0PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        // 2. Verify the assignment was successful.
        waitForView(allOf(segment0PlayerName, withText(profileName)))
        onView(segment0PlayerName).check(matches(withText(profileName)))


        // --- ACT ---
        // 3. Navigate to the Reset Game dialog and perform the reset.
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Reset Game"), click()
            ))

        // "Reset current" is selected by default, so we just need to click the positive button.
        onView(withText("Reset")).perform(click())
        pressBack()

        // --- ASSERT ---
        // 4. Wait for the player name to revert to its default state after the reset.
        waitForView(allOf(segment0PlayerName, withText("Player 1")))

        // 5. Verify the entire player segment has been reverted to its default state.
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        onView(segment0Matcher).check(matches(withBackgroundColor(R.color.default_segment_background)))
    }

    @Test
    fun changingStartingLife_clearsProfileAssignments() {
        // --- ARRANGE ---
        // 1. Create a profile and assign it to Player 1.
        val profileName = "TestProfile"
        createTestProfile(profileName, "#4CAF50") // A green color

        val segment0Matcher = withTagValue(equalTo("player_segment_0"))
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment0Matcher))
        val segment0PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment0Matcher))

        onView(segment0PlayerName).perform(directlyPerformClick())
        waitForView(segment0PopupRecycler)
        onView(segment0PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        // 2. Verify the assignment was successful.
        waitForView(allOf(segment0PlayerName, withText(profileName)))
        onView(segment0PlayerName).check(matches(withText(profileName)))

        // --- ACT ---
        // 3. Navigate to the Starting Life dialog and change the life total.
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Starting Life")).perform(click())
        onView(withText("20")).perform(click()) // Change to 20 life
        pressBack()

        // --- ASSERT ---
        // 4. Wait for the player name to revert to its default state after the reset.
        waitForView(allOf(segment0PlayerName, withText("Player 1")))

        // 5. Verify the player segment has fully reverted to its default state.
        onView(segment0PlayerName).check(matches(withText("Player 1")))
        onView(segment0Matcher).check(matches(withBackgroundColor(R.color.default_segment_background)))
    }

    @Test
    fun selectingStaleProfile_whenAlreadyAssignedByOtherPlayer_showsSnackbar() {
        // --- ARRANGE ---
        // 1. Create a single profile that both players will try to select.
        val profileName = "TestProfile"
        createTestProfile(profileName)

        // 2. Define matchers for both players.
        val segment0Matcher = withTagValue(equalTo("player_segment_0"))
        val segment0PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment0Matcher))
        val segment0PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment0Matcher))

        val segment1Matcher = withTagValue(equalTo("player_segment_1"))
        val segment1PlayerName = allOf(withId(R.id.tv_player_name), isDescendantOfA(segment1Matcher))
        val segment1PopupRecycler = allOf(withId(R.id.profiles_recycler_view), isDescendantOfA(segment1Matcher))

        // 3. Open the profile popup for BOTH players. Player 1's popup will become "stale".
        onView(segment0PlayerName).perform(directlyPerformClick())
        onView(segment1PlayerName).perform(directlyPerformClick())

        // 4. Verify the profile is initially visible in both popups.
        waitForView(allOf(withText(profileName), isDescendantOfA(segment0PopupRecycler)))
        waitForView(allOf(withText(profileName), isDescendantOfA(segment1PopupRecycler)))

        // --- ACT ---
        // 5. Player 2 selects the profile first.
        onView(segment1PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        // 6. Verify Player 2's segment updates correctly.
        waitForView(allOf(segment1PlayerName, withText(profileName)))
        onView(segment1PlayerName).check(matches(withText(profileName)))

        // 7. Now, Player 1 (with the stale popup) tries to select the same profile.
        onView(segment0PopupRecycler)
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(profileName)), directlyPerformClick()
            ))

        // --- ASSERT ---
        // 8. A Snackbar should appear, telling Player 1 the profile is already in use.
        onView(withText("'$profileName' is already in use."))
            .check(matches(isDisplayed()))

        // 9. Crucially, Player 1's name should NOT have changed.
        onView(segment0PlayerName).check(matches(withText("Player 1")))
    }
}