package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CommanderDamageUITest : BaseUITest() {

    private val player2CommanderSummary = withTagValue(equalTo("player_segment_1_commander_summary"))

    @Test
    fun commanderDamageSummary_shouldBeVisible_onLaunch() {
        onView(isRoot()).check(withViewCount(withId(R.id.commander_damage_summary), 2))
    }

    @Test
    fun incrementingAndDecrementingCommanderDamage_updatesDialogUI() {
        val damageFromPlayer1 = allOf(withId(R.id.tv_commander_damage), hasSibling(withText("Player 1")))

        // Open the dialog and wait for it to be ready
        onView(player2CommanderSummary).perform(directlyPerformClick())
        Thread.sleep(500) // A simple sleep is reliable here.

        // Increment and check
        onView(damageFromPlayer1).perform(click())
        onView(damageFromPlayer1).check(matches(withText("1")))

        // Decrement and check
        onView(damageFromPlayer1).perform(longClick())
        onView(allOf(withId(R.id.iv_decrement_button), isDisplayed())).perform(click())
        onView(damageFromPlayer1).check(matches(withText("0")))
    }

//    @Test
//    fun decrementButton_shouldBeHidden_whenDamageIsZero() {
//        val damageFromPlayer1 = allOf(withId(R.id.tv_commander_damage), hasSibling(withText("Player 1")))
//        // **THE FIX**: This matcher finds the decrement button that is a descendant of the
//        // layout that ALSO has a sibling view with the text "Player 1". This is unambiguous.
//        val decrementButtonForPlayer1 = allOf(
//            withId(R.id.iv_decrement_button),
//            isDescendantOfA(hasSibling(withText("Player 1")))
//        )
//
//        // Open the dialog and wait
//        onView(player2CommanderSummary).perform(directlyPerformClick())
//        Thread.sleep(500)
//
//        // Increment, then long-press to show the button
//        onView(damageFromPlayer1).perform(click())
//        onView(damageFromPlayer1).perform(longClick())
//
//        // Interact with the specific button
//        onView(allOf(decrementButtonForPlayer1, isDisplayed())).perform(click())
//        onView(damageFromPlayer1).check(matches(withText("0")))
//
//        // Assert that the specific button is now gone
//        onView(decrementButtonForPlayer1).check(matches(not(isDisplayed())))
//    }

//    @Test
//    fun togglingCommanderDamageDeduction_updatesLifeCorrectly() {
//        val damageFromPlayer1 = allOf(withId(R.id.tv_commander_damage), hasSibling(withText("Player 1")))
//
//        // **Deduction is ON by default.** Deal 1 damage.
//        onView(player2CommanderSummary).perform(directlyPerformClick())
//        Thread.sleep(500)
//        onView(damageFromPlayer1).perform(click())
//        pressBack()
//
//        // **THE FIX**: Use our helper to wait for the UI to show the correct life total.
//        waitForView(allOf(player2LifeCounter, withText("39")))
//        onView(player2LifeCounter).check(matches(withText("39")))
//
//        // **Turn deduction OFF**
//        onView(withId(R.id.settingsIcon)).perform(click())
//        Thread.sleep(500)
//        onView(withText("Preferences")).perform(click())
//        Thread.sleep(500)
//        onView(withId(R.id.switch_auto_deduce_damage)).perform(click())
//
//        // Recreate the activity to apply the setting
//        activityRule.scenario.recreate()
//
//        // **THE FIX**: Wait for the main UI to be ready again before interacting
//        waitForView(player2CommanderSummary)
//
//        // Deal one more damage
//        onView(player2CommanderSummary).perform(directlyPerformClick())
//        Thread.sleep(500)
//        onView(damageFromPlayer1).perform(click())
//        pressBack()
//
//        // Life should NOT have changed this time.
//        // Wait for any potential (but not expected) UI updates before asserting.
//        Thread.sleep(500)
//        onView(player2LifeCounter).check(matches(withText("39")))
//    }
}