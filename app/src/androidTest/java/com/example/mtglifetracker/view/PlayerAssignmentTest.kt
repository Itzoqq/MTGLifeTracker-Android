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
import com.example.mtglifetracker.directlyPerformClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerAssignmentTest : BaseUITest() {

    @Suppress("SameParameterValue")
    private fun createTestProfile(nickname: String) {
        onView(withId(R.id.settingsIcon)).perform(click())

        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Manage Profiles"), click()
            ))

        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText(nickname))
        onView(withText("Save")).perform(click())

        pressBack()
        pressBack()
    }

    @Test
    fun clickingPlayerName_onRotatedSegment_shouldOpenProfileSelectionPopup() {
        // Arrange
        createTestProfile("TestProfile")

        // Act: Target the player name TextView and use the direct click action
        val rotatedSegmentMatcher = withTagValue(equalTo("player_segment_0"))
        val playerNameMatcher = allOf(
            withId(R.id.tv_player_name),
            isDescendantOfA(rotatedSegmentMatcher)
        )
        onView(playerNameMatcher).perform(directlyPerformClick())

        // Assert
        val profilePopupMatcher = allOf(
            withId(R.id.profile_popup_container),
            isDescendantOfA(rotatedSegmentMatcher)
        )
        onView(profilePopupMatcher).check(matches(isDisplayed()))
    }

    @Test
    fun clickingPlayerName_onNonRotatedSegment_shouldOpenProfileSelectionPopup() {
        // Arrange
        createTestProfile("TestProfile")

        // Act: Target the player name TextView and use the direct click action
        val nonRotatedSegmentMatcher = withTagValue(equalTo("player_segment_1"))
        val playerNameMatcher = allOf(
            withId(R.id.tv_player_name),
            isDescendantOfA(nonRotatedSegmentMatcher)
        )
        onView(playerNameMatcher).perform(directlyPerformClick())

        // Assert
        val profilePopupMatcher = allOf(
            withId(R.id.profile_popup_container),
            isDescendantOfA(nonRotatedSegmentMatcher)
        )
        onView(profilePopupMatcher).check(matches(isDisplayed()))
    }
}