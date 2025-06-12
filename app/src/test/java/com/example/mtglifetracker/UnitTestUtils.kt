package com.example.mtglifetracker

import android.view.MotionEvent

/**
 * A collection of utility functions for local unit tests.
 */

// A reusable helper to create MotionEvent instances for testing views.
fun createMotionEvent(action: Int, x: Float, y: Float): MotionEvent {
    return MotionEvent.obtain(0, 0, action, x, y, 0)
}