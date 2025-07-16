package com.example.mtglifetracker.view

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
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
 * Local unit tests for the [ColorAdapter].
 *
 * This class uses Robolectric to create a real Android environment (including a hosting
 * Activity and RecyclerView) to test the adapter's logic for item creation, click handling,
 * and selection state management. It uses a mock callback to verify interactions.
 */
@Config(sdk = [34])
class ColorAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: ColorAdapter
    private val colors = listOf("#F44336", "#2196F3", "#4CAF50")
    // A mock function to verify that the adapter's callback is invoked correctly.
    private lateinit var onColorSelected: (String?) -> Unit

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
        onColorSelected = mock()
        adapter = ColorAdapter(colors, onColorSelected)

        // Set up the hosting activity and RecyclerView
        activityController = Robolectric.buildActivity(AppCompatActivity::class.java).create().visible()
        val activity = activityController.get()
        recyclerView = RecyclerView(activity)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        activity.setContentView(recyclerView)

        // Manually trigger a measure and layout pass. This is crucial in Robolectric tests
        // to ensure the ViewHolders are created and are available to be found by the tests.
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
     * Tests that the adapter reports the correct number of items.
     */
    @Test
    fun item_count_should_return_correct_number_of_colors() {
        Logger.unit("TEST_START: item_count_should_return_correct_number_of_colors")
        // Act & Assert
        assertEquals(3, adapter.itemCount)
        Logger.unit("TEST_PASS: item_count_should_return_correct_number_of_colors")
    }

    /**
     * Tests that clicking a color swatch triggers the `onColorSelected` callback
     * with the correct color string.
     */
    @Test
    fun clicking_color_should_trigger_onColorSelected_callback_with_correct_color() {
        Logger.unit("TEST_START: clicking_color_should_trigger_onColorSelected_callback_with_correct_color")
        // Arrange
        val positionToClick = 1
        val expectedColor = colors[positionToClick]
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)

        // Act
        Logger.unit("Act: Performing click on item at position $positionToClick.")
        viewHolder!!.itemView.performClick()

        // Assert
        Logger.unit("Assert: Verifying callback was invoked with color '$expectedColor'.")
        verify(onColorSelected).invoke(expectedColor)
        Logger.unit("TEST_PASS: clicking_color_should_trigger_onColorSelected_callback_with_correct_color")
    }

    /**
     * Tests that clicking an already selected color swatch triggers the callback with `null`,
     * effectively clearing the selection.
     */
    @Test
    fun clicking_selected_color_should_trigger_onColorSelected_with_null() {
        Logger.unit("TEST_START: clicking_selected_color_should_trigger_onColorSelected_with_null")
        // Arrange
        val positionToClick = 0
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)!!

        // Act: Click the same item twice.
        Logger.unit("Act: Performing first click on item at position $positionToClick.")
        viewHolder.itemView.performClick() // First click selects it.
        Logger.unit("Act: Performing second click on the same item.")
        viewHolder.itemView.performClick() // Second click deselects it.

        // Assert
        Logger.unit("Assert: Verifying callback was invoked first with the color, then with null.")
        verify(onColorSelected).invoke(colors[positionToClick])
        verify(onColorSelected).invoke(null)
        Logger.unit("TEST_PASS: clicking_selected_color_should_trigger_onColorSelected_with_null")
    }

    /**
     * Tests that changing the selection from one color to another triggers the callbacks correctly.
     */
    @Test
    fun changing_selection_should_trigger_callbacks_correctly() {
        Logger.unit("TEST_START: changing_selection_should_trigger_callbacks_correctly")
        // Arrange
        val viewHolder0 = recyclerView.findViewHolderForAdapterPosition(0)!!
        val viewHolder1 = recyclerView.findViewHolderForAdapterPosition(1)!!

        // Act: Click the first item, then the second item.
        Logger.unit("Act: Clicking item 0, then item 1.")
        viewHolder0.itemView.performClick()
        viewHolder1.itemView.performClick()

        // Assert
        Logger.unit("Assert: Verifying callback was invoked for both colors in order.")
        verify(onColorSelected).invoke(colors[0])
        verify(onColorSelected).invoke(colors[1])
        Logger.unit("TEST_PASS: changing_selection_should_trigger_callbacks_correctly")
    }

    /**
     * Tests that programmatically setting the selected color correctly updates the adapter's
     * internal state, allowing the next click to deselect it.
     */
    @Test
    fun setSelectedColor_should_correctly_update_internal_state() {
        Logger.unit("TEST_START: setSelectedColor_should_correctly_update_internal_state")
        // Arrange
        Logger.unit("Arrange: Programmatically setting selected color to item 1.")
        adapter.setSelectedColor(colors[1])
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(1)!!

        // Act: Click the already-selected item.
        Logger.unit("Act: Clicking the programmatically selected item.")
        viewHolder.itemView.performClick()

        // Assert: The callback should be invoked with null because the item was deselected.
        Logger.unit("Assert: Verifying callback was invoked with null.")
        verify(onColorSelected).invoke(null)
        Logger.unit("TEST_PASS: setSelectedColor_should_correctly_update_internal_state")
    }
}