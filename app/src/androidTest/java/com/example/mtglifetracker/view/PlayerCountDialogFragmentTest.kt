package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
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
        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 2))

        onView(withId(R.id.settingsIcon)).perform(click())

        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Number of Players"), click()
            ))

        onView(withText("4")).perform(click())

        pressBack()

        onView(isRoot()).check(withViewCount(withId(R.id.lifeCounter), 4))
    }
}