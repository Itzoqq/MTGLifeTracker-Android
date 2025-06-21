package com.example.mtglifetracker.view

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.ThemedRobolectricTest
import com.example.mtglifetracker.model.Profile
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@Config(sdk = [34])
class ProfilePopupAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: ProfilePopupAdapter
    private val profiles = listOf(
        Profile(id = 1, nickname = "PopupOne", color = null),
        Profile(id = 2, nickname = "PopupTwo", color = "#00FF00")
    )
    private lateinit var onProfileClicked: (Profile) -> Unit

    private lateinit var activityController: ActivityController<AppCompatActivity>
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        onProfileClicked = mock()
        adapter = ProfilePopupAdapter(profiles, onProfileClicked)

        activityController = Robolectric.buildActivity(AppCompatActivity::class.java).create().visible()
        val activity = activityController.get()

        recyclerView = RecyclerView(activity)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        activity.setContentView(recyclerView)

        // Force layout pass
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(480, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY)
        )
        recyclerView.layout(0, 0, 480, 800)
    }

    @After
    fun tearDown() {
        activityController.destroy()
    }

    @Test
    fun item_count_should_return_correct_number_of_profiles() {
        assertEquals(profiles.size, adapter.itemCount)
    }

    @Test
    fun clicking_item_should_trigger_onProfileClicked_callback_with_correct_profile() {
        val positionToClick = 1
        val expectedProfile = profiles[positionToClick]

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)
        viewHolder!!.itemView.performClick()

        verify(onProfileClicked).invoke(expectedProfile)
    }

    @Test
    fun bind_should_set_correct_text_for_item() {
        val position = 0
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)!!

        // The layout R.layout.list_item_popup uses android.R.id.text1
        val textView = viewHolder.itemView.findViewById<TextView>(android.R.id.text1)

        assertEquals(profiles[position].nickname, textView.text.toString())
    }
}