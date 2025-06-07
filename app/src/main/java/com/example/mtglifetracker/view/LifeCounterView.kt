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

    var onLifeIncreasedListener: (() -> Unit)? = null
    var onLifeDecreasedListener: (() -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val decrease = when (rotation) {
                0f -> event.x < width / 2
                180f -> event.x > width / 2
                90f -> event.y < height / 2
                -90f, 270f -> event.y > height / 2
                else -> false
            }

            if (decrease) {
                onLifeDecreasedListener?.invoke()
            } else {
                onLifeIncreasedListener?.invoke()
            }

            // FIXED: Calling performClick() is essential to notify the system
            // about the click for accessibility and proper UI updates.
            performClick()

            return true
        }
        return super.onTouchEvent(event)
    }

    // It's good practice to also override performClick and call the superclass's method.
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}