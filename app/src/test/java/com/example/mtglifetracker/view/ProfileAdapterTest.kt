package com.example.mtglifetracker.view

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.ThemedRobolectricTest
import com.example.mtglifetracker.model.Profile
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
 * Local unit tests for the [ProfileAdapter].
 *
 * This class uses Robolectric to test the adapter in a realistic Android environment.
 * It verifies that the adapter correctly displays profile data, including showing or hiding
 * the color swatch, and that it correctly handles long-click interactions to trigger its callback.
 */
@Config(sdk = [34])
class ProfileAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: ProfileAdapter
    private val profilesWithColor = listOf(
        Profile(id = 1, nickname = "PlayerOne", color = "#FF0000"),
        Profile(id = 2, nickname = "PlayerTwo", color = null)
    )
    // A mock function to verify that the adapter's callback is invoked correctly.
    private lateinit var onProfileClicked: (Profile) -> Unit

    private lateinit var activityController: ActivityController<AppCompatActivity>
    private lateinit var recyclerView: RecyclerView

    /**
     * Sets up the test environment before each test.
     * This method initializes the adapter, mock callback, and a hosting RecyclerView
     * within a simple activity.
     */
    @Before
    fun setUp() {
        Logger.unit("TEST_SETUP: Starting...")
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

        // Submit the list to the adapter, since it's a ListAdapter.
        Logger.unit("TEST_SETUP: Submitting list of ${profilesWithColor.size} profiles to the adapter.")
        adapter.submitList(profilesWithColor)

        // Force a layout pass to ensure ViewHolders are created and findable.
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(480, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY)
        )
        recyclerView.layout(0, 0, 480, 800)
        Logger.unit("TEST_SETUP: Complete. RecyclerView is ready.")
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
     * Tests that the adapter reports the correct number of items after a list is submitted.
     */
    @Test
    fun item_count_should_reflect_submitted_list_size() {
        Logger.unit("TEST_START: item_count_should_reflect_submitted_list_size")
        // Act & Assert
        Logger.unit("Assert: Verifying item count is 2.")
        assertEquals(2, adapter.itemCount)
        Logger.unit("TEST_PASS: item_count_should_reflect_submitted_list_size")
    }

    /**
     * Tests that long-clicking an item correctly triggers the `onProfileClicked` callback
     * with the corresponding profile data.
     */
    @Test
    fun long_clicking_item_should_trigger_onProfileClicked_callback() {
        Logger.unit("TEST_START: long_clicking_item_should_trigger_onProfileClicked_callback")
        // Arrange
        val positionToClick = 0
        val expectedProfile = profilesWithColor[positionToClick]
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)

        // Act
        Logger.unit("Act: Performing long-click on item at position $positionToClick.")
        viewHolder!!.itemView.performLongClick()

        // Assert
        Logger.unit("Assert: Verifying callback was invoked with profile '${expectedProfile.nickname}'.")
        verify(onProfileClicked).invoke(expectedProfile)
        Logger.unit("TEST_PASS: long_clicking_item_should_trigger_onProfileClicked_callback")
    }

    /**
     * Tests that the ViewHolder correctly shows the color dot when the profile's color is not null.
     */
    @Test
    fun bind_should_show_color_dot_when_color_is_not_null() {
        Logger.unit("TEST_START: bind_should_show_color_dot_when_color_is_not_null")
        // Arrange
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)!!
        val colorView = viewHolder.itemView.findViewById<View>(R.id.view_profile_color)
        val nicknameView = viewHolder.itemView.findViewById<TextView>(R.id.tv_profile_nickname)

        // Assert
        Logger.unit("Assert: Verifying color dot is VISIBLE and nickname is correct.")
        assertEquals(View.VISIBLE, colorView.visibility)
        assertEquals("PlayerOne", nicknameView.text)
        Logger.unit("TEST_PASS: bind_should_show_color_dot_when_color_is_not_null")
    }

    /**
     * Tests that the ViewHolder correctly hides the color dot when the profile's color is null.
     */
    @Test
    fun bind_should_hide_color_dot_when_color_is_null() {
        Logger.unit("TEST_START: bind_should_hide_color_dot_when_color_is_null")
        // Arrange
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(1)!!
        val colorView = viewHolder.itemView.findViewById<View>(R.id.view_profile_color)
        val nicknameView = viewHolder.itemView.findViewById<TextView>(R.id.tv_profile_nickname)

        // Assert
        Logger.unit("Assert: Verifying color dot is INVISIBLE and nickname is correct.")
        assertEquals(View.INVISIBLE, colorView.visibility)
        assertEquals("PlayerTwo", nicknameView.text)
        Logger.unit("TEST_PASS: bind_should_hide_color_dot_when_color_is_null")
    }
}