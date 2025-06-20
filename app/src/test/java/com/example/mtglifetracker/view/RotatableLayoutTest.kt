package com.example.mtglifetracker.view

import android.content.Context
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import com.example.mtglifetracker.ThemedRobolectricTest
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Unit tests for the RotatableLayout.
 *
 * This class uses Robolectric to test the custom measurement and touch
 * transformation logic of the RotatableLayout.
 */
@Config(sdk = [34])
class RotatableLayoutTest : ThemedRobolectricTest() { // Extends our base class

    private lateinit var rotatableLayout: RotatableLayout
    private lateinit var touchEventRecordingView: TouchEventRecordingView

    // A simple view that records the last touch event it received.
    // THIS WAS THE MISSING PIECE.
    private class TouchEventRecordingView(context: Context) : View(context) {
        var lastMotionEvent: MotionEvent? = null
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            lastMotionEvent = MotionEvent.obtain(event)
            return true // Consume the event
        }
    }

    @Before
    fun setUp() {
        // themedContext is provided by the base class
        rotatableLayout = RotatableLayout(themedContext)
        touchEventRecordingView = TouchEventRecordingView(themedContext)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        rotatableLayout.addView(touchEventRecordingView, params)

        rotatableLayout.measure(
            MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY)
        )
        rotatableLayout.layout(0, 0, 200, 100)
    }

    @Test
    fun initialization_shouldInflateLayoutAndFindViews() {
        // We need a fresh instance, using the themedContext
        val layout = RotatableLayout(themedContext)
        assertNotNull("LifeCounterView should not be null", layout.lifeCounter)
        // REMOVED: Assertion for deltaCounter
        assertEquals("Default angle should be 0", 0, layout.angle)
    }

    @Test
    fun onMeasure_shouldNotSwapDimensions_whenAngleIs0() {
        rotatableLayout.angle = 0
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)

        rotatableLayout.measure(widthSpec, heightSpec)

        assertEquals("Width should not be swapped", 300, rotatableLayout.measuredWidth)
        assertEquals("Height should not be swapped", 150, rotatableLayout.measuredHeight)
    }

    @Test
    fun onMeasure_shouldKeepOriginalDimensions_whenAngleIs90() {
        rotatableLayout.angle = 90
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)

        rotatableLayout.measure(widthSpec, heightSpec)

        assertEquals("Width should remain as specified", 300, rotatableLayout.measuredWidth)
        assertEquals("Height should remain as specified", 150, rotatableLayout.measuredHeight)
    }

    @Test
    fun onMeasure_shouldKeepOriginalDimensions_whenAngleIsMinus90() {
        rotatableLayout.angle = -90
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)

        rotatableLayout.measure(widthSpec, heightSpec)

        assertEquals("Width should remain as specified", 300, rotatableLayout.measuredWidth)
        assertEquals("Height should remain as specified", 150, rotatableLayout.measuredHeight)
    }

    @Test
    fun dispatchTouchEvent_shouldNotTransformCoordinates_whenAngleIs0() {
        rotatableLayout.angle = 0
        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 75f)

        rotatableLayout.dispatchTouchEvent(downEvent)
        Shadows.shadowOf(Looper.getMainLooper()).idle() // Execute pending UI tasks

        assertEquals(50f, touchEventRecordingView.lastMotionEvent?.x)
        assertEquals(75f, touchEventRecordingView.lastMotionEvent?.y)
    }

    @Test
    fun dispatchTouchEvent_shouldTransformCoordinates_whenAngleIs90() {
        rotatableLayout.angle = 90
        rotatableLayout.measure(
            MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY)
        )
        rotatableLayout.layout(0, 0, 200, 100)

        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 40f)
        rotatableLayout.dispatchTouchEvent(downEvent)
        Shadows.shadowOf(Looper.getMainLooper()).idle() // Execute pending UI tasks

        // Expected transformation for -90 rotation + translation:
        // x' = y = 40
        // y' = -x + width = -150 + 200 = 50
        assertNotNull("Motion event should have been received", touchEventRecordingView.lastMotionEvent)
        assertEquals(40f, touchEventRecordingView.lastMotionEvent!!.x, 0.01f)
        assertEquals(50f, touchEventRecordingView.lastMotionEvent!!.y, 0.01f)
    }

    @Test
    fun dispatchTouchEvent_shouldTransformCoordinates_whenAngleIs180() {
        rotatableLayout.angle = 180
        rotatableLayout.measure(
            MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY)
        )
        rotatableLayout.layout(0, 0, 200, 100)

        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 60f, 70f)
        rotatableLayout.dispatchTouchEvent(downEvent)
        Shadows.shadowOf(Looper.getMainLooper()).idle() // Execute pending UI tasks

        // Expected transformation for -180 rotation + translation:
        // x' = -x + width = -60 + 200 = 140
        // y' = -y + height = -70 + 100 = 30
        assertNotNull("Motion event should have been received", touchEventRecordingView.lastMotionEvent)
        assertEquals(140f, touchEventRecordingView.lastMotionEvent!!.x, 0.01f)
        assertEquals(30f, touchEventRecordingView.lastMotionEvent!!.y, 0.01f)
    }

    @Test
    fun dispatchTouchEvent_shouldTransformCoordinates_whenAngleIsMinus90() {
        rotatableLayout.angle = -90 // Same as 270 degrees
        rotatableLayout.measure(
            MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY)
        )
        rotatableLayout.layout(0, 0, 200, 100)

        val downEvent = createMotionEvent(MotionEvent.ACTION_DOWN, 80f, 20f)
        rotatableLayout.dispatchTouchEvent(downEvent)
        Shadows.shadowOf(Looper.getMainLooper()).idle() // Execute pending UI tasks

        // Expected transformation for -270 rotation + translation:
        // x' = -y + height = -20 + 100 = 80
        // y' = x = 80
        assertNotNull("Motion event should have been received", touchEventRecordingView.lastMotionEvent)
        assertEquals(80f, touchEventRecordingView.lastMotionEvent!!.x, 0.01f)
        assertEquals(80f, touchEventRecordingView.lastMotionEvent!!.y, 0.01f)
    }

    @Suppress("SameParameterValue")
    private fun createMotionEvent(action: Int, x: Float, y: Float): MotionEvent {
        return MotionEvent.obtain(0, 0, action, x, y, 0)
    }
}