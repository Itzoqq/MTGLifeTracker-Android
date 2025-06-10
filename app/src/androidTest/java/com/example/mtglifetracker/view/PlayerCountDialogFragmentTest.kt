package com.example.mtglifetracker.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.data.AppDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for the `PlayerCountDialogFragment`.
 *
 * This class tests the full user flow of changing the player count,
 * from opening the settings dialog to verifying the main UI update.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerCountDialogFragmentTest {

    /**
     * Hilt rule to set up the Hilt dependency injection framework for the test.
     * It must be ordered to run before the ActivityScenarioRule.
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * ActivityScenarioRule launches the MainActivity before each test.
     * This provides a consistent starting point for UI tests.
     */
    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * This method runs after each test to clear all data from the database.
     * This ensures that each test starts with a fresh, default state.
     */
    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        db.clearAllTables()
        activityRule.scenario.recreate()
    }

    /**
     * Tests the entire flow of changing the number of players from the default (2) to 4.
     * It verifies that the dialog appears, can be interacted with, and that the
     * main screen's layout updates correctly after a selection is made.
     */
    @Test
    fun selectingPlayerCount_updatesTheGameScreen() {
        // Arrange: Verify the app starts with the default 2 players.
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 2))

        // Act: Navigate through the settings to change the player count.
        // 1. Click the settings icon in the center of the screen.
        onView(withId(R.id.settingsIcon)).perform(click())

        // 2. In the settings dialog, click the "Number of Players" option.
        onView(withText("Number of Players")).perform(click())

        // 3. In the player count dialog, click on the option for "4" players.
        //    This also implicitly asserts that the dialog and the option are visible.
        onView(withText("4")).perform(click())

        // Assert:
        // 1. The player count dialog should no longer be on the screen.
        onView(withText("Number of Players")).check(doesNotExist())

        // 2. The main screen should now contain exactly 4 LifeCounterViews.
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 4))
    }

    /**
     * A custom Espresso ViewAssertion to check for a specific number of views
     * that match a given matcher within the current view hierarchy.
     *
     * @param matcher The matcher to identify the views to count.
     * @param expectedCount The expected number of views.
     * @return A ViewAssertion that performs the count check.
     */
    private fun withViewCount(matcher: Matcher<View>, expectedCount: Int): ViewAssertion {
        return ViewAssertion { view, noViewFoundException ->
            // If the root view is not found, re-throw the exception.
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            // A custom matcher to find and count all views that match the given criteria.
            val viewMatcher = object : TypeSafeMatcher<View>() {
                var actualCount = 0

                override fun describeTo(description: Description) {
                    description.appendText("Found $actualCount views matching $matcher")
                }

                override fun matchesSafely(item: View): Boolean {
                    // This matcher is designed to run against the root view and traverse its children.
                    // It always returns false itself but uses its internal traversal to count.
                    actualCount = countMatchingViews(item, matcher)
                    return actualCount == expectedCount
                }
            }

            // Assert that the number of matching views is correct.
            if (!viewMatcher.matches(view)) {
                throw AssertionFailedError(
                    "Expected $expectedCount views matching $matcher, but found ${viewMatcher.actualCount}"
                )
            }
        }
    }

    /**
     * Recursively traverses a view hierarchy and counts the number of views
     * that match the given matcher.
     *
     * @param view The root view to start the search from.
     * @param matcher The matcher to apply to each view.
     * @return The total number of matching views found.
     */
    private fun countMatchingViews(view: View, matcher: Matcher<View>): Int {
        var count = 0
        if (matcher.matches(view)) {
            count++
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                count += countMatchingViews(view.getChildAt(i), matcher)
            }
        }
        return count
    }
}
