package com.example.mtglifetracker

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import com.example.mtglifetracker.util.Logger
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher

/**
 * A custom Espresso [ViewAction] to perform a click at a specific horizontal percentage of a view.
 *
 * This is useful for views that have different functionality based on where they are clicked,
 * such as the [com.example.mtglifetracker.view.LifeCounterView] where the left half decrements
 * life and the right half increments it.
 *
 * @param pct The horizontal percentage (from 0 to 100) where the click should occur.
 * @return A [ViewAction] that can be passed to `onView().perform()`.
 */
fun clickInXPercent(pct: Int): ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        { view ->
            // Calculate the absolute screen coordinates for the click.
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)
            val screenX = screenPos[0] + (view.width * pct / 100f)
            val screenY = screenPos[1] + (view.height / 2f) // Click in the vertical center.
            Logger.instrumented("clickInXPercent: Clicking at $pct% -> coordinates ($screenX, $screenY).")
            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )
}

/**
 * A custom Espresso [ViewAssertion] to check for a specific number of views
 * that match a given matcher within the current view hierarchy.
 *
 * This should be applied to a root view, like `onView(isRoot())`.
 * It's useful for verifying that a layout has been updated correctly after an action,
 * for example, checking that there are 4 player segments after changing the player count.
 *
 * @param viewMatcher The matcher to identify the views to count.
 * @param expectedCount The exact number of views that are expected to be found.
 * @return A [ViewAssertion] that performs the count check.
 */
fun withViewCount(viewMatcher: Matcher<View>, expectedCount: Int): ViewAssertion {
    return ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) {
            // If the root view itself isn't found, re-throw the exception.
            throw noViewFoundException
        }

        // Recursively search the view hierarchy starting from the given root view.
        val actualCount = countMatchingViews(view, viewMatcher)
        Logger.instrumented("withViewCount: Checking for matcher '$viewMatcher'. Expected: $expectedCount, Found: $actualCount.")

        // If the actual count does not match the expected count, fail the test.
        if (actualCount != expectedCount) {
            throw AssertionFailedError(
                "Expected $expectedCount views matching '$viewMatcher', but found $actualCount"
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

    // If the view is a ViewGroup, recursively check all of its children.
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            count += countMatchingViews(view.getChildAt(i), matcher)
        }
    }
    return count
}

/**
 * A custom Espresso [ViewAction] that directly calls the `performClick()` method on a view.
 *
 * This should be used as a last resort when default Espresso click actions fail due
 * to complex layouts, custom touch event interception (like in [com.example.mtglifetracker.view.PlayerSegmentView]),
 * or animation issues. It bypasses the normal touch event injection system and directly
 * invokes the view's `OnClickListener`.
 *
 * @return A [ViewAction] that directly performs a click.
 */
fun directlyPerformClick(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            // The only constraint for this action is that the view must be enabled.
            return isEnabled()
        }

        override fun getDescription(): String {
            return "directly call performClick() on view"
        }

        override fun perform(uiController: UiController, view: View) {
            Logger.instrumented("directlyPerformClick: Performing direct click on view with ID: ${view.id}")
            // Directly call the view's performClick(), which will trigger its OnClickListener.
            view.performClick()
        }
    }
}