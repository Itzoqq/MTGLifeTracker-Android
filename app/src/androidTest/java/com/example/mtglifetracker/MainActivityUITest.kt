package com.example.mtglifetracker

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.RotatableLayout
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun settingsIcon_shouldBeDisplayed_onLaunch() {
        onView(withId(R.id.settingsIcon)).check(matches(isDisplayed()))
    }

    @Test
    fun lifeTotal_shouldChange_andDeltaShouldAppear_whenPlayerSegmentIsClicked() {
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withAngle(0))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withAngle(0))
        )

        onView(lifeCounterMatcher).perform(clickInXPercent(25))
        onView(lifeCounterMatcher).check(matches(withText("39")))
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("-1"))))

        onView(lifeCounterMatcher).perform(clickInXPercent(75))
        onView(lifeCounterMatcher).check(matches(withText("40")))
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("0"))))
    }

    @Test
    fun playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())

        onView(isRoot()).check(matches(hasViewCount(LifeCounterView::class.java, 4)))
    }

    private fun clickInXPercent(pct: Int): ViewAction {
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

    @Suppress("SameParameterValue")
    private fun hasViewCount(viewClass: Class<out View>, expectedCount: Int): Matcher<View> {
        return object : org.hamcrest.BaseMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("has $expectedCount views of class ${viewClass.simpleName}")
            }

            override fun matches(item: Any?): Boolean {
                val rootView = item as? View ?: return false
                val views = ArrayList<View>()
                rootView.findViewsWithText(views, "40", View.FIND_VIEWS_WITH_TEXT)
                return views.filterIsInstance(viewClass).size == expectedCount
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun withAngle(expectedAngle: Int): Matcher<View> {
        return object : BoundedMatcher<View, RotatableLayout>(RotatableLayout::class.java) {
            // FIX: Redundant qualifier removed
            override fun describeTo(description: Description) {
                description.appendText("with angle: $expectedAngle")
            }

            override fun matchesSafely(item: RotatableLayout): Boolean {
                return item.angle == expectedAngle
            }
        }
    }
}