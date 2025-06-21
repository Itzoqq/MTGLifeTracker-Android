package com.example.mtglifetracker.view

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.ThemedRobolectricTest
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
class SettingsAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: SettingsAdapter
    private val options = arrayOf("Number of Players", "Starting Life", "Manage Profiles", "Reset Game")
    private lateinit var onItemClicked: (Int) -> Unit

    private lateinit var activityController: ActivityController<AppCompatActivity>
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        // Mock the callback lambda
        onItemClicked = mock()
        adapter = SettingsAdapter(options, onItemClicked)

        // Set up the hosting activity and RecyclerView
        activityController = Robolectric.buildActivity(AppCompatActivity::class.java).create().visible()
        val activity = activityController.get()

        recyclerView = RecyclerView(activity)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        activity.setContentView(recyclerView)

        // Force a layout pass to ensure ViewHolders are created and findable
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
    fun item_count_should_return_correct_number_of_options() {
        assertEquals(options.size, adapter.itemCount)
    }

    @Test
    fun clicking_item_should_trigger_onItemClicked_callback_with_correct_position() {
        val positionToClick = 2 // "Manage Profiles"

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)
        viewHolder!!.itemView.performClick()

        // Verify the mock was called with the correct position index
        verify(onItemClicked).invoke(positionToClick)
    }

    @Test
    fun bind_should_set_correct_text_for_item() {
        val positionToCheck = 1 // "Starting Life"
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToCheck)!!

        // The layout used is android.R.layout.simple_list_item_1, so the ID is android.R.id.text1
        val textView = viewHolder.itemView.findViewById<TextView>(android.R.id.text1)

        assertEquals(options[positionToCheck], textView.text.toString())
    }
}