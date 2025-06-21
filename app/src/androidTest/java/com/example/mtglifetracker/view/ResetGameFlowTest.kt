package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.clickInXPercent
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ResetGameFlowTest : BaseUITest() {

    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    private fun openResetDialog() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Reset Game"), click()
            ))
    }

    @Test
    fun resetCurrentGame_shouldOnlyResetCurrentGameLifeTotals() {
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        openResetDialog()
        onView(withId(R.id.rb_reset_current)).perform(click())
        onView(withText("Reset")).perform(click())

        pressBack()

        onView(lifeCounterForPlayer2).check(matches(withText("40")))
    }

    @Test
    fun resetAllGames_shouldResetAllGameLifeTotals() {
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        openResetDialog()
        onView(withId(R.id.rb_reset_all)).perform(click())
        onView(withText("Reset")).perform(click())

        pressBack()

        onView(lifeCounterForPlayer2).check(matches(withText("40")))
    }

    @Test
    fun cancelReset_shouldNotChangeLifeTotals() {
        onView(lifeCounterForPlayer2).perform(clickInXPercent(25))
        onView(lifeCounterForPlayer2).check(matches(withText("39")))

        openResetDialog()
        onView(withText("Cancel")).perform(click())

        pressBack()

        onView(lifeCounterForPlayer2).check(matches(withText("39")))
    }
}