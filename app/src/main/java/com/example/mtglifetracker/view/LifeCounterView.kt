package com.example.mtglifetracker.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

/**
 * A custom TextView designed to act as a life counter button. It detects touches
 * on its left and right halves to signal life decrease or increase events.
 *
 * It now supports both single taps and press-and-hold gestures. Holding down
 * will continuously change the life total at an accelerating rate.
 */
class LifeCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /** A lambda that is invoked when the "increase life" area is tapped or held. */
    var onLifeIncreasedListener: (() -> Unit)? = null
    /** A lambda that is invoked when the "decrease life" area is tapped or held. */
    var onLifeDecreasedListener: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isHeldDown = false
    private var isIncreasing = false

    // Configuration for the hold-to-update feature
    private val initialDelay = 400L  // Time to wait before continuous updates start
    private val startInterval = 150L // The initial interval between updates
    private val minInterval = 50L    // The fastest interval for updates
    private val accelerationRate = 10L  // How much to decrease the interval on each step (in ms)
    private var currentInterval = startInterval

    /**
     * This Runnable is defined as an object to correctly scope `this` in the postDelayed call.
     * It checks the `isIncreasing` flag to determine whether to increment or decrement.
     */
    private val continuousUpdateRunnable = object : Runnable {
        override fun run() {
            if (!isHeldDown) {
                stopContinuousUpdates()
                return
            }

            // Trigger the appropriate life change based on the flag
            if (isIncreasing) {
                onLifeIncreasedListener?.invoke()
            } else {
                onLifeDecreasedListener?.invoke()
            }

            // Accelerate the update rate
            currentInterval = (currentInterval - accelerationRate).coerceAtLeast(minInterval)

            // Schedule the next update by passing a reference to this runnable
            handler.postDelayed(this, currentInterval)
        }
    }

    /**
     * Overrides the default touch event handling to implement custom click and hold logic.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isHeldDown = true
                val isLeftSide = event.x < width / 2
                isIncreasing = !isLeftSide // Set the direction flag

                // Immediately trigger the first change
                if (isIncreasing) {
                    onLifeIncreasedListener?.invoke()
                } else {
                    onLifeDecreasedListener?.invoke()
                }

                // Start the continuous update runnable after an initial delay
                handler.postDelayed(continuousUpdateRunnable, initialDelay)

                performClick()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHeldDown = false
                // Stop the continuous updates when the finger is lifted
                stopContinuousUpdates()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Stops the continuous update runnable and resets the update interval.
     */
    private fun stopContinuousUpdates() {
        handler.removeCallbacks(continuousUpdateRunnable)
        currentInterval = startInterval
    }

    /**
     * Overriding performClick is good practice for custom touch handling.
     * We call the superclass' method to ensure default accessibility behaviors are triggered.
     */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}