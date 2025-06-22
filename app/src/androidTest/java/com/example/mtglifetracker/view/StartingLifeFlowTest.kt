package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class StartingLifeFlowTest : BaseUITest() {

    private val lifeCounterForPlayer2 = allOf(
        withId(R.id.lifeCounter),
        isDescendantOfA(withTagValue(equalTo("player_segment_1")))
    )

    private fun openStartingLifeDialog() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Starting Life"), click()
            ))
    }

    @Test
    fun selectingPresetStartingLife_shouldUpdateLifeOnScreen() {
        onView(lifeCounterForPlayer2).check(matches(withText("40")))

        openStartingLifeDialog()
        onView(withText("20")).perform(click())

        pressBack()

        onView(lifeCounterForPlayer2).check(matches(withText("20")))
    }

    @Test
    fun selectingCustomStartingLife_shouldUpdateLifeOnScreen() {
        onView(lifeCounterForPlayer2).check(matches(withText("40")))

        openStartingLifeDialog()
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("123"))
        onView(withText("Set")).perform(click())

        pressBack()

        onView(lifeCounterForPlayer2).check(matches(withText("123")))
    }

    @Test
    fun enteringInvalidCustomLife_shouldNotUpdateLife() {
        // Arrange
        openStartingLifeDialog()
        onView(withText("20")).perform(click())
        pressBack()
        onView(lifeCounterForPlayer2).check(matches(withText("20")))

        // Act
        openStartingLifeDialog()
        onView(withText("Custom")).perform(click())
        onView(withId(R.id.et_custom_life)).perform(replaceText("0"))
        onView(withText("Set")).perform(click())

        // Assert: Dialog should still be open
        onView(withText(R.string.title_custom_starting_life)).check(matches(isDisplayed()))

        // FIX: Close the dialogs. One pressBack now dismisses all of them.
        pressBack()

        // Assert: Life total is unchanged
        onView(lifeCounterForPlayer2).check(matches(withText("20")))
    }

    @Test
    fun enteringEmptyCustomLife_shouldNotDismissDialog() {
        // Arrange: Navigate to the custom life dialog.
        openStartingLifeDialog()
        onView(withText("Custom")).perform(click())

        // Act: Ensure the input is empty and click "Set".
        onView(withId(R.id.et_custom_life)).perform(replaceText(""))
        onView(withText("Set")).perform(click())

        // Assert: The dialog should remain open because the empty input is not valid.
        onView(withText(R.string.title_custom_starting_life)).check(matches(isDisplayed()))
    }
}