package com.example.mtglifetracker

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.view.LifeCounterView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityUITest : BaseUITest() {

    @Test
    fun settingsIcon_shouldBeDisplayed_onLaunch() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Assert
        onView(withId(R.id.settingsIcon)).check(matches(isDisplayed()))
    }

    @Test
    fun lifeTotal_shouldChange_andDeltaShouldAppear_whenPlayerSegmentIsClicked() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Matchers
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // Act & Assert Decrease
        onView(lifeCounterMatcher).perform(forceClickInXPercent(75))
        onView(lifeCounterMatcher).check(matches(withText("39")))
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("-1"))))

        // Act & Assert Increase
        onView(lifeCounterMatcher).perform(forceClickInXPercent(25))
        onView(lifeCounterMatcher).check(matches(withText("40")))
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("0"))))
    }

    @Test
    fun playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())

        // Assert
        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 4))
    }

    @Test
    fun lifeIncrease_shouldShowPositiveDeltaColor() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Matchers
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_1")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_1")))
        )

        // Act
        onView(lifeCounterMatcher).perform(forceClickInXPercent(75))

        // Assert
        onView(deltaCounterMatcher).check(matches(allOf(
            isDisplayed(),
            withText("+1"),
            withTextColor(R.color.delta_positive)
        )))
    }

    @Test
    fun lifeDecrease_shouldShowNegativeDeltaColor() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Matchers
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // Act
        onView(lifeCounterMatcher).perform(forceClickInXPercent(75))

        // Assert
        onView(deltaCounterMatcher).check(matches(allOf(
            isDisplayed(),
            withText("-1"),
            withTextColor(R.color.delta_negative)
        )))
    }

    @Test
    fun deltaCounter_shouldDisappear_afterTimeout() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // Matchers
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // Act
        onView(lifeCounterMatcher).perform(forceClickInXPercent(25))
        onView(deltaCounterMatcher).check(matches(isDisplayed()))

        // This sleep is acceptable here because we are testing a delayed action.
        Thread.sleep(3100)

        // Assert
        onView(deltaCounterMatcher).check(matches(not(isDisplayed())))
    }

    @Test
    fun layout_shouldHaveWiderBias_for5PlayerGame() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // 1. Arrange: Change player count to 5
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("5")).perform(click())

        // 2. Act: Click a life counter to make the delta appear
        val lifeCounterToClick = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_2")))
        )
        onView(lifeCounterToClick).perform(forceClickInXPercent(25))

        // 3. Assert: Check the bias on the now-visible delta counter.
        val deltaCounterToCheck = allOf(
            withId(R.id.deltaCounter),
            withEffectiveVisibility(Visibility.VISIBLE)
        )
        onView(deltaCounterToCheck).check { view, _ ->
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            assertEquals(0.75f, params.horizontalBias)
        }
    }

    @Test
    fun layout_shouldHaveWiderBias_for6PlayerGame() {
        // Sanity check
        onView(withId(R.id.main_container)).check(matches(isDisplayed()))

        // 1. Arrange: Change player count to 6
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("6")).perform(click())

        // 2. Act: Click a life counter to make the delta appear
        val lifeCounterToClick = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        onView(lifeCounterToClick).perform(forceClickInXPercent(75))

        // 3. Assert: Check the bias on the now-visible delta counter.
        val deltaCounterToCheck = allOf(
            withId(R.id.deltaCounter),
            withEffectiveVisibility(Visibility.VISIBLE)
        )
        onView(deltaCounterToCheck).check { view, _ ->
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            assertEquals(0.75f, params.horizontalBias)
        }
    }
}