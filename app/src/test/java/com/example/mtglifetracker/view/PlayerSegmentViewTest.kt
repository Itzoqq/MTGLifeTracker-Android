package com.example.mtglifetracker.view

import android.view.MotionEvent
import android.view.View.MeasureSpec
import com.example.mtglifetracker.ThemedRobolectricTest
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Unit tests for the PlayerSegmentView.
 *
 * This test class directly invokes the view's transformation logic
 * to ensure it is correct, avoiding the complexities of touch dispatching.
 */
@Config(sdk = [34])
class PlayerSegmentViewTest : ThemedRobolectricTest() {

    private lateinit var playerSegmentView: PlayerSegmentView

    @Before
    fun setUp() {
        playerSegmentView = PlayerSegmentView(themedContext)

        // Measure and lay out the view so it has dimensions (width and height).
        playerSegmentView.measure(
            MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY)
        )
        playerSegmentView.layout(0, 0, 200, 100)
    }

    @Test
    fun initialization_shouldInflateLayoutAndFindViews() {
        val layout = PlayerSegmentView(themedContext)
        assertNotNull("LifeCounterView should not be null", layout.lifeCounter)
        assertNotNull("PlayerName TextView should not be null", layout.playerName)
        assertEquals("Default angle should be 0", 0, layout.angle)
    }

    // --- Measurement Tests ---

    @Test
    fun onMeasure_shouldNotSwapDimensions_whenAngleIs0() {
        playerSegmentView.angle = 0
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)

        playerSegmentView.measure(widthSpec, heightSpec)

        assertEquals("Width should not be swapped", 300, playerSegmentView.measuredWidth)
        assertEquals("Height should not be swapped", 150, playerSegmentView.measuredHeight)
    }

    @Test
    fun onMeasure_shouldKeepOriginalDimensions_whenAngleIs90() {
        playerSegmentView.angle = 90
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)

        playerSegmentView.measure(widthSpec, heightSpec)

        assertEquals("Width should remain as specified", 300, playerSegmentView.measuredWidth)
        assertEquals("Height should remain as specified", 150, playerSegmentView.measuredHeight)
    }

    @Test
    fun onMeasure_shouldKeepOriginalDimensions_whenAngleIsMinus90() {
        playerSegmentView.angle = -90
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)

        playerSegmentView.measure(widthSpec, heightSpec)

        assertEquals("Width should remain as specified", 300, playerSegmentView.measuredWidth)
        assertEquals("Height should remain as specified", 150, playerSegmentView.measuredHeight)
    }

    // --- Transformation Logic Tests ---

    @Test
    fun transformEvent_shouldNotTransformCoordinates_whenAngleIs0() {
        playerSegmentView.angle = 0
        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 75f)

        playerSegmentView.transformEvent(downEvent)

        assertEquals(50f, downEvent.x, 0.01f)
        assertEquals(75f, downEvent.y, 0.01f)
    }


    @Test
    fun transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs90() {
        playerSegmentView.angle = 90
        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 40f)

        playerSegmentView.transformEvent(downEvent)

        // Expected transformation for -90 rotation + translation:
        // x' = y = 40
        // y' = -x + width = -150 + 200 = 50
        assertEquals(40f, downEvent.x, 0.01f)
        assertEquals(50f, downEvent.y, 0.01f)
    }

    @Test
    fun transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs180() {
        playerSegmentView.angle = 180
        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 60f, 70f)

        playerSegmentView.transformEvent(downEvent)

        // Expected transformation for -180 rotation + translation:
        // x' = -x + width = -60 + 200 = 140
        // y' = -y + height = -70 + 100 = 30
        assertEquals(140f, downEvent.x, 0.01f)
        assertEquals(30f, downEvent.y, 0.01f)
    }

    @Test
    fun transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIsMinus90() {
        playerSegmentView.angle = -90 // Same as 270 degrees
        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 80f, 20f)

        playerSegmentView.transformEvent(downEvent)

        // Expected transformation for -270 rotation + translation:
        // x' = -y + height = -20 + 100 = 80
        // y' = x = 80
        assertEquals(80f, downEvent.x, 0.01f)
        assertEquals(80f, downEvent.y, 0.01f)
    }

    @Suppress("SameParameterValue")
    private fun createMotionEvent(action: Int, x: Float, y: Float): MotionEvent {
        return MotionEvent.obtain(0, 0, action, x, y, 0)
    }
}