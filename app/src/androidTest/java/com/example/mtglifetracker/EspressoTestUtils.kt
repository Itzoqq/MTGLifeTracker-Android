package com.example.mtglifetracker

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import android.view.ViewGroup
import androidx.test.espresso.ViewAssertion
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
import android.os.SystemClock
import androidx.test.espresso.UiController
import androidx.test.espresso.matcher.ViewMatchers.isEnabled

// A reusable custom matcher to check the text color of a TextView.
fun withTextColor(expectedColorId: Int): Matcher<View> {
    return object : BoundedMatcher<View, TextView>(TextView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with text color from resource id: $expectedColorId")
        }

        override fun matchesSafely(item: TextView): Boolean {
            val context = item.context
            val expectedColor = ContextCompat.getColor(context, expectedColorId)
            return item.currentTextColor == expectedColor
        }
    }
}


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

fun forceClickInXPercent(pct: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            // Looser constraint: The view only needs to be enabled.
            return isEnabled()
        }

        override fun getDescription(): String {
            return "force click at horizontal $pct percent"
        }

        override fun perform(uiController: UiController, view: View) {
            // Get coordinates for the click
            val coordinates = IntArray(2)
            view.getLocationOnScreen(coordinates)
            val x = coordinates[0] + (view.width * pct / 100f)
            val y = coordinates[1] + (view.height / 2f)

            // Create and inject the touch event
            val downEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0)
            val upEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0)

            try {
                uiController.injectMotionEvent(downEvent)
                uiController.injectMotionEvent(upEvent)
                uiController.loopMainThreadUntilIdle()
            } finally {
                downEvent.recycle()
                upEvent.recycle()
            }
        }
    }
}