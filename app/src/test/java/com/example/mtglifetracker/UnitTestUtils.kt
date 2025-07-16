package com.example.mtglifetracker

import android.view.MotionEvent
import com.example.mtglifetracker.util.Logger

/**
 * A collection of utility functions for local unit tests, specifically for creating test objects.
 */

/**
 * A reusable helper function to create [MotionEvent] instances for testing views.
 *
 * This simplifies the process of creating MotionEvent objects in view-related unit tests,
 * especially those that rely on Robolectric.
 *
 * @param action The motion event action, such as [MotionEvent.ACTION_DOWN] or [MotionEvent.ACTION_UP].
 * @param x The x-coordinate for the event.
 * @param y The y-coordinate for the event.
 * @return A new [MotionEvent] instance.
 */
fun createMotionEvent(action: Int, x: Float, y: Float): MotionEvent {
    // This log is useful for debugging tests that simulate complex touch interactions.
    Logger.unit("UnitTestUtils: Creating MotionEvent with action $action at (x=$x, y=$y).")
    return MotionEvent.obtain(0, 0, action, x, y, 0)
}