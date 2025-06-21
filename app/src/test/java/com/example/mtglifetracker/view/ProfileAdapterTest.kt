package com.example.mtglifetracker.view

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
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
class ProfileAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: ProfileAdapter
    private val profilesWithColor = listOf(
        Profile(id = 1, nickname = "PlayerOne", color = "#FF0000"),
        Profile(id = 2, nickname = "PlayerTwo", color = null)
    )
    private lateinit var onProfileClicked: (Profile) -> Unit

    private lateinit var activityController: ActivityController<AppCompatActivity>
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        // Mock the callback to verify interactions
        onProfileClicked = mock()
        adapter = ProfileAdapter(onProfileClicked)

        // Set up the activity and RecyclerView
        activityController = Robolectric.buildActivity(AppCompatActivity::class.java).create().visible()
        val activity = activityController.get()

        recyclerView = RecyclerView(activity)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        activity.setContentView(recyclerView)

        // Submit the list to the adapter, since it's a ListAdapter
        adapter.submitList(profilesWithColor)

        // Force a layout pass to ensure ViewHolders are created
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
    fun item_count_should_reflect_submitted_list_size() {
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun long_clicking_item_should_trigger_onProfileClicked_callback() {
        val positionToClick = 0
        val expectedProfile = profilesWithColor[positionToClick]

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)
        viewHolder!!.itemView.performLongClick()

        // Verify the callback was invoked with the correct profile
        verify(onProfileClicked).invoke(expectedProfile)
    }

    @Test
    fun bind_should_show_color_dot_when_color_is_not_null() {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)!!
        val colorView = viewHolder.itemView.findViewById<View>(R.id.view_profile_color)
        val nicknameView = viewHolder.itemView.findViewById<TextView>(R.id.tv_profile_nickname)

        // Assert that the color dot is visible and the name is correct
        assertEquals(View.VISIBLE, colorView.visibility)
        assertEquals("PlayerOne", nicknameView.text)
    }

    @Test
    fun bind_should_hide_color_dot_when_color_is_null() {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(1)!!
        val colorView = viewHolder.itemView.findViewById<View>(R.id.view_profile_color)
        val nicknameView = viewHolder.itemView.findViewById<TextView>(R.id.tv_profile_nickname)

        // Assert that the color dot is invisible and the name is correct
        assertEquals(View.INVISIBLE, colorView.visibility)
        assertEquals("PlayerTwo", nicknameView.text)
    }
}