package com.example.mtglifetracker.view

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
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
class ColorAdapterTest : ThemedRobolectricTest() {

    private lateinit var adapter: ColorAdapter
    private val colors = listOf("#F44336", "#2196F3", "#4CAF50")
    private lateinit var onColorSelected: (String?) -> Unit

    private lateinit var activityController: ActivityController<AppCompatActivity>
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        onColorSelected = mock()
        adapter = ColorAdapter(colors, onColorSelected)

        activityController = Robolectric.buildActivity(AppCompatActivity::class.java).create().visible()
        val activity = activityController.get()

        recyclerView = RecyclerView(activity)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        activity.setContentView(recyclerView)

        // **THE FIX**: Manually trigger a measure and layout pass.
        // This ensures the ViewHolders are created and available to be found by the tests.
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
    fun item_count_should_return_correct_number_of_colors() {
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun clicking_color_should_trigger_onColorSelected_callback_with_correct_color() {
        val positionToClick = 1
        val expectedColor = colors[positionToClick]

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)
        viewHolder!!.itemView.performClick()

        verify(onColorSelected).invoke(expectedColor)
    }

    @Test
    fun clicking_selected_color_should_trigger_onColorSelected_with_null() {
        val positionToClick = 0

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick)!!
        viewHolder.itemView.performClick() // First click
        viewHolder.itemView.performClick() // Second click

        verify(onColorSelected).invoke(colors[positionToClick])
        verify(onColorSelected).invoke(null)
    }

    @Test
    fun changing_selection_should_trigger_callbacks_correctly() {
        val viewHolder0 = recyclerView.findViewHolderForAdapterPosition(0)!!
        val viewHolder1 = recyclerView.findViewHolderForAdapterPosition(1)!!

        viewHolder0.itemView.performClick()
        viewHolder1.itemView.performClick()

        verify(onColorSelected).invoke(colors[0])
        verify(onColorSelected).invoke(colors[1])
    }

    @Test
    fun setSelectedColor_should_correctly_update_internal_state() {
        adapter.setSelectedColor(colors[1])

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(1)!!
        viewHolder.itemView.performClick()

        verify(onColorSelected).invoke(null)
    }
}