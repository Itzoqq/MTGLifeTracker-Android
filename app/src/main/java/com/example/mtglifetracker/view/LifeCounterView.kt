package com.example.mtglifetracker.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

class LifeCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // Listeners to communicate life changes back to the Activity
    var onLifeIncreasedListener: (() -> Unit)? = null
    var onLifeDecreasedListener: (() -> Unit)? = null

    init {
        // Ensure the view is clickable so it can receive focus and accessibility events
        isClickable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // We are only interested in the initial press down event
        if (event.action == MotionEvent.ACTION_DOWN) {
            // This logic determines if the touch was on the "decrease" or "increase" side,
            // taking the view's rotation into account to match the visual layout.
            val decrease: Boolean
            val touchX = event.x
            val touchY = event.y

            decrease = when (rotation) {
                0f -> touchX < width / 2                 // Left half
                180f -> touchX > width / 2                // Visually left half (due to rotation)
                90f -> touchY < height / 2                // Visually top half
                -90f, 270f -> touchY > height / 2     // Visually top half (due to rotation)
                else -> false
            }

            if (decrease) {
                onLifeDecreasedListener?.invoke()
            } else {
                onLifeIncreasedListener?.invoke()
            }

            // By calling performClick, we properly handle accessibility events (like TalkBack)
            // and provide visual feedback like the ripple effect. This is key to fixing the warnings.
            performClick()
        }
        // Return true to indicate that we have handled the touch event.
        return super.onTouchEvent(event)
    }

    // Overriding performClick is good practice for custom touch handling.
    // We call super.performClick() to ensure default behavior (like accessibility announcements) happens.
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}