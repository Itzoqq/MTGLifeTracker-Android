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
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher

// A reusable ViewAction to click on a view at a specific horizontal percentage.
fun clickInXPercent(pct: Int): ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)
            val screenX = screenPos[0] + (view.width * pct / 100f)
            val screenY = screenPos[1] + (view.height / 2f)
            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )
}

/**
 * A custom Espresso ViewAssertion to check for a specific number of views
 * that match a given matcher within the current view hierarchy.
 *
 * This should be applied to a root view, like `onView(isRoot())`.
 *
 * @param viewMatcher The matcher to identify the views to count.
 * @param expectedCount The expected number of views.
 * @return A ViewAssertion that performs the count check.
 */
fun withViewCount(viewMatcher: Matcher<View>, expectedCount: Int): ViewAssertion {
    return ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) {
            throw noViewFoundException
        }

        val actualCount = countMatchingViews(view, viewMatcher)
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

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            count += countMatchingViews(view.getChildAt(i), matcher)
        }
    }
    return count
}

/**
 * A powerful ViewAction that directly calls `performClick()` on a view.
 * This should be used as a last resort when default click actions fail due
 * to complex layouts, custom touch intercept logic, or animation issues.
 * It does not simulate a user tap, but directly invokes the view's OnClickListener.
 */
fun directlyPerformClick(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            // The only constraint is that the view is enabled.
            return isEnabled()
        }

        override fun getDescription(): String {
            return "directly call performClick() on view"
        }

        override fun perform(uiController: UiController, view: View) {
            // Directly call performClick(), which will trigger the OnClickListener.
            view.performClick()
        }
    }
}