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

}