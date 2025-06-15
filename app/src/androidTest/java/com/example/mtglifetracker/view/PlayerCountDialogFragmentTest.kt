package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.withViewCount
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerCountDialogFragmentTest : BaseUITest() {

    @Test
    fun selectingPlayerCount_updatesTheGameScreen() {
        // Sanity check to ensure we start in a known state (2 players)
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 2))

        // Act
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Number of Players")).perform(click())
        onView(withText("4")).perform(click())

        // Assert: The dialog should be gone and the player count updated
        onView(withText("Number of Players")).check(doesNotExist())
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 4))
    }
}