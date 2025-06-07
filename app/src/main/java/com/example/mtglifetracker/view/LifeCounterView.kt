package com.example.mtglifetracker.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

/**
 * A custom TextView designed to act as a life counter button. It detects touches
 * on its left and right halves to signal life decrease or increase events.
 */
class LifeCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /** A lambda that is invoked when the "increase life" area is tapped. */
    var onLifeIncreasedListener: (() -> Unit)? = null
    /** A lambda that is invoked when the "decrease life" area is tapped. */
    var onLifeDecreasedListener: (() -> Unit)? = null

    /**
     * Overrides the default touch event handling to implement custom click logic.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // We only trigger the action on the initial press down for immediate feedback.
        if (event.action == MotionEvent.ACTION_DOWN) {

            // If the touch is on the first half of the view's width,
            // it's a decrease, otherwise it's an increase. Because the parent RotatableLayout
            // transforms the touch coordinates, this logic works correctly regardless of rotation.
            val isLeftSide = event.x < width / 2

            if (isLeftSide) {
                onLifeDecreasedListener?.invoke()
            } else {
                onLifeIncreasedListener?.invoke()
            }

            // Calling performClick() is essential to notify the system for accessibility
            // and to trigger standard view feedback like sound or haptics.
            performClick()

            // Return true to indicate that this view has handled and consumed the touch event.
            return true
        }
        return super.onTouchEvent(event)
    }

    /**
     * Overriding performClick is good practice for custom touch handling.
     * We call the superclass's method to ensure default accessibility behaviors are triggered.
     */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}