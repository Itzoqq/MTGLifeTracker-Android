package com.example.mtglifetracker.view

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
 * Local unit tests for the [ProfilePopupAdapter].
 *
 * This class uses Robolectric to test the adapter in a simulated Android environment,
 * complete with a hosting Activity and RecyclerView. It verifies that the adapter
 * correctly displays profile data and handles click events by invoking its callback.
 */
@Config(sdk = [34])
class ProfilePopupAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: ProfilePopupAdapter
    private val profiles = listOf(
        Profile(id = 1, nickname = "PopupOne", color = null),
        Profile(id = 2, nickname = "PopupTwo", color = "#00FF00")
    )
    // A mock function to verify that the adapter's callback is invoked correctly.
    private lateinit var onProfileClicked: (Profile) -> Unit

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
        onProfileClicked = mock()
        adapter = ProfilePopupAdapter(profiles, onProfileClicked)

        // Set up the activity and RecyclerView
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
     * Tests that the adapter reports the correct number of items.
     */
    @Test
    fun item_count_should_return_correct_number_of_profiles() {
        Logger.unit("TEST_START: item_count_should_return_correct_number_of_profiles")
        // Act & Assert
        Logger.unit("Assert: Verifying item count is ${profiles.size}.")
        assertEquals(profiles.size, adapter.itemCount)
        Logger.unit("TEST_PASS: item_count_should_return_correct_number_of_profiles")
    }

    /**
     * Tests that clicking an item correctly triggers the `onProfileClicked` callback
     * with the corresponding profile data.
     */
    @Test
    fun clicking_item_should_trigger_onProfileClicked_callback_with_correct_profile() {
        Logger.unit("TEST_START: clicking_item_should_trigger_onProfileClicked_callback_with_correct_profile")
        // Arrange
        val positionToClick = 1
        val expectedProfile = profiles[positionToClick]
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)

        // Act
        Logger.unit("Act: Performing click on item at position $positionToClick.")
        viewHolder!!.itemView.performClick()

        // Assert
        Logger.unit("Assert: Verifying callback was invoked with profile '${expectedProfile.nickname}'.")
        verify(onProfileClicked).invoke(expectedProfile)
        Logger.unit("TEST_PASS: clicking_item_should_trigger_onProfileClicked_callback_with_correct_profile")
    }

    /**
     * Tests that the ViewHolder correctly binds the profile's nickname to the TextView.
     */
    @Test
    fun bind_should_set_correct_text_for_item() {
        Logger.unit("TEST_START: bind_should_set_correct_text_for_item")
        // Arrange
        val position = 0
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)!!
        // The layout R.layout.list_item_popup uses android.R.id.text1 for its TextView.
        val textView = viewHolder.itemView.findViewById<TextView>(android.R.id.text1)

        // Assert
        Logger.unit("Assert: Verifying text for item at position $position.")
        assertEquals(profiles[position].nickname, textView.text.toString())
        Logger.unit("TEST_PASS: bind_should_set_correct_text_for_item")
    }
}