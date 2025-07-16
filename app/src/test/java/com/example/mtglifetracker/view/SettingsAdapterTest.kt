package com.example.mtglifetracker.view

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.ThemedRobolectricTest
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

/**
 * Local unit tests for the [SettingsAdapter].
 *
 * This class uses Robolectric to create a real Android environment to test the
 * adapter's logic. It verifies that the adapter correctly displays its options and
 * that click events on its items trigger the appropriate callback with the correct position.
 */
@Config(sdk = [34])
class SettingsAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: SettingsAdapter
    private val options = arrayOf("Number of Players", "Starting Life", "Manage Profiles", "Reset Game")
    // A mock function to verify that the adapter's callback is invoked correctly.
    private lateinit var onItemClicked: (Int) -> Unit

    private lateinit var activityController: ActivityController<AppCompatActivity>
    private lateinit var recyclerView: RecyclerView

    /**
     * Sets up the test environment before each test.
     * This method initializes the mock callback, the adapter, and the hosting RecyclerView
     * within a simple AppCompatActivity.
     */
    @Before
    fun setUp() {
        Logger.unit("TEST_SETUP: Starting...")
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

        // Force a layout pass to ensure ViewHolders are created and findable.
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(480, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY)
        )
        recyclerView.layout(0, 0, 480, 800)
        Logger.unit("TEST_SETUP: Complete. RecyclerView and adapter are ready.")
    }

    /**
     * Cleans up the test environment after each test by destroying the activity.
     */
    @After
    fun tearDown() {
        Logger.unit("TEST_TEARDOWN: Destroying activity controller.")
        activityController.destroy()
    }

    /**
     * Tests that the adapter reports the correct number of items based on the options array.
     */
    @Test
    fun item_count_should_return_correct_number_of_options() {
        Logger.unit("TEST_START: item_count_should_return_correct_number_of_options")
        // Act & Assert
        Logger.unit("Assert: Verifying item count is ${options.size}.")
        assertEquals(options.size, adapter.itemCount)
        Logger.unit("TEST_PASS: item_count_should_return_correct_number_of_options")
    }

    /**
     * Tests that clicking an item triggers the `onItemClicked` callback with the correct position index.
     */
    @Test
    fun clicking_item_should_trigger_onItemClicked_callback_with_correct_position() {
        Logger.unit("TEST_START: clicking_item_should_trigger_onItemClicked_callback_with_correct_position")
        // Arrange
        val positionToClick = 2 // "Manage Profiles"
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)

        // Act
        Logger.unit("Act: Performing click on item at position $positionToClick.")
        viewHolder!!.itemView.performClick()

        // Assert
        Logger.unit("Assert: Verifying callback was invoked with position $positionToClick.")
        // Verify the mock was called with the correct position index.
        verify(onItemClicked).invoke(positionToClick)
        Logger.unit("TEST_PASS: clicking_item_should_trigger_onItemClicked_callback_with_correct_position")
    }

    /**
     * Tests that the ViewHolder correctly binds the option text to the TextView.
     */
    @Test
    fun bind_should_set_correct_text_for_item() {
        Logger.unit("TEST_START: bind_should_set_correct_text_for_item")
        // Arrange
        val positionToCheck = 1 // "Starting Life"
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToCheck)!!
        // The layout used is android.R.layout.simple_list_item_1, so the ID is android.R.id.text1.
        val textView = viewHolder.itemView.findViewById<TextView>(android.R.id.text1)

        // Assert
        Logger.unit("Assert: Verifying text for item at position $positionToCheck.")
        assertEquals(options[positionToCheck], textView.text.toString())
        Logger.unit("TEST_PASS: bind_should_set_correct_text_for_item")
    }
}