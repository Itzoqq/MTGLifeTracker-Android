package com.example.mtglifetracker

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
import org.hamcrest.Matchers.not
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
        // Note: The delta text is now "0" but the view itself may still be visible briefly.
        // A better check might be for the text or its visibility after a delay.
        // For this test, we'll assume checking the text is sufficient.
        onView(deltaCounterMatcher).check(matches(allOf(isDisplayed(), withText("0"))))
    }

    @Test
    fun playerLayout_shouldUpdate_whenPlayerCountIsChangedViaSettings() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())

        // --- THIS IS THE CORRECTED LINE ---
        // We now pass a Matcher and use withViewCount directly in check()
        onView(isRoot()).check(withViewCount(isAssignableFrom(LifeCounterView::class.java), 4))
    }

    @Test
    fun lifeIncrease_shouldShowPositiveDeltaColor() {
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withAngle(0))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withAngle(0))
        )

        onView(lifeCounterMatcher).perform(clickInXPercent(75))

        onView(deltaCounterMatcher).check(matches(allOf(
            isDisplayed(),
            withText("+1"),
            withTextColor(R.color.delta_positive)
        )))
    }

    @Test
    fun lifeDecrease_shouldShowNegativeDeltaColor() {
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withAngle(0))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withAngle(0))
        )

        onView(lifeCounterMatcher).perform(clickInXPercent(25))

        onView(deltaCounterMatcher).check(matches(allOf(
            isDisplayed(),
            withText("-1"),
            withTextColor(R.color.delta_negative)
        )))
    }

    @Test
    fun deltaCounter_shouldDisappear_afterTimeout() {
        val lifeCounterMatcher = allOf(
            withId(R.id.lifeCounter),
            isDescendantOfA(withAngle(0))
        )
        val deltaCounterMatcher = allOf(
            withId(R.id.deltaCounter),
            isDescendantOfA(withAngle(0))
        )

        onView(lifeCounterMatcher).perform(clickInXPercent(25))
        onView(deltaCounterMatcher).check(matches(isDisplayed()))

        Thread.sleep(3000)

        onView(deltaCounterMatcher).check(matches(not(isDisplayed())))
    }
}