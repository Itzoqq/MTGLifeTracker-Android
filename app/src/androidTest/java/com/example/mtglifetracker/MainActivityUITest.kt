package com.example.mtglifetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.view.LifeCounterView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityUITest : BaseUITest() {

    @Test
    fun settingsIcon_shouldBeDisplayed_onLaunch() {
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))
        onView(withId(R.id.settingsIcon)).check(matches(isDisplayed()))
    }

    @Test
    fun lifeTotal_shouldChange_whenPlayerSegmentIsClicked() {
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // For the 180-degree rotated view (player_segment_0), the touch coordinates are inverted.
        // A click on the left half of the screen (25%) is registered on the right half of the view.

        // Act & Assert Increase (by clicking the "left" side of the screen)
        onView(lifeCounterMatcher).perform(clickInXPercent(25))
        onView(lifeCounterMatcher).check(matches(withText("41")))

        // Act & Assert Decrease (by clicking the "right" side of the screen)
        onView(lifeCounterMatcher).perform(clickInXPercent(75))
        onView(lifeCounterMatcher).check(matches(withText("40")))
    }

    @Test
    fun playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings() {
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())

        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 4))
    }
}