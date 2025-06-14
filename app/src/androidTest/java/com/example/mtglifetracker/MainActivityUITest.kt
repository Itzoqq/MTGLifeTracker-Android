package com.example.mtglifetracker

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.view.LifeCounterView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val clearDatabaseRule = DatabaseClearingRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun settingsIcon_shouldBeDisplayed_onLaunch() {
        onView(withId(R.id.settingsIcon)).check(matches(isDisplayed()))
    }

    @Test
    fun lifeTotal_shouldChange_andDeltaShouldAppear_whenPlayerSegmentIsClicked() {
        // This matcher targets player 1's segment (index 0), which is rotated 180 degrees.
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // FIX: Click right side (75%) to trigger a "decrease" on a 180-degree rotated view.
        onView(lifeCounterMatcher).perform(forceClickInXPercent(75))
        onView(lifeCounterMatcher).check(matches(withText("39")))
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("-1"))))

        // FIX: Click left side (25%) to trigger an "increase" on a 180-degree rotated view.
        onView(lifeCounterMatcher).perform(forceClickInXPercent(25))
        onView(lifeCounterMatcher).check(matches(withText("40")))
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("0"))))
    }

    @Test
    fun playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())

        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 4))
    }

    @Test
    fun lifeIncrease_shouldShowPositiveDeltaColor() {
        // REFACTORED: This matcher now explicitly targets player 2's segment (index 1).
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_1")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_1")))
        )

        onView(lifeCounterMatcher).perform(forceClickInXPercent(75))

        onView(deltaCounterMatcher).check(matches(allOf(
            isDisplayed(),
            withText("+1"),
            withTextColor(R.color.delta_positive)
        )))
    }

    @Test
    fun lifeDecrease_shouldShowNegativeDeltaColor() {
        // This matcher targets player 1's segment (index 0), which is rotated 180 degrees.
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        // FIX: Click right side (75%) to trigger a "decrease" on a 180-degree rotated view.
        onView(lifeCounterMatcher).perform(forceClickInXPercent(75))

        onView(deltaCounterMatcher).check(matches(allOf(
            isDisplayed(),
            withText("-1"),
            withTextColor(R.color.delta_negative)
        )))
    }

    @Test
    fun deltaCounter_shouldDisappear_afterTimeout() {
        // REFACTORED: This matcher now explicitly targets player 1's segment (index 0).
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withTagValue(equalTo("player_segment_0")))
        )

        onView(lifeCounterMatcher).perform(forceClickInXPercent(25))
        onView(deltaCounterMatcher).check(matches(isDisplayed()))

        // This sleep is acceptable for testing a delayed action.
        Thread.sleep(3100)

        onView(deltaCounterMatcher).check(matches(not(isDisplayed())))
    }

    @Test
    fun layout_shouldHaveWiderBias_for5PlayerGame() {
        // 1. Arrange: Change player count to 5
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("5")).perform(click())

        // 2. Act: Click a life counter using the "force" action.
        // We use the tag to uniquely identify player segment 2.
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
        // 1. Arrange: Change player count to 6
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("6")).perform(click())

        // 2. Act: Click a life counter using the "force" action.
        // We use the tag to uniquely identify player segment 0.
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