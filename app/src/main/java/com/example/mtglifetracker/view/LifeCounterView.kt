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
            // The logic is now always the same, regardless of rotation, because
            // the parent RotatableLayout has already transformed the coordinates.
            val isLeftSide = event.x < width / 2

            if (isLeftSide) {
                onLifeDecreasedListener?.invoke()
            } else {
                onLifeIncreasedListener?.invoke()
            }

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