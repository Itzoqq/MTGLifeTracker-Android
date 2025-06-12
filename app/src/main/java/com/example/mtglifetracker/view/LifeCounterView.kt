package com.example.mtglifetracker.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible

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

    private val dismissableOverlays = mutableListOf<View>()

    // Configuration for the hold-to-update feature
    private val initialDelay = 400L  // Time to wait before continuous updates start
    private val startInterval = 150L // The initial interval between updates
    private val minInterval = 50L    // The fastest interval for updates
    private val accelerationRate = 10L  // How much to decrease the interval on each step (in ms)
    private var currentInterval = startInterval

    fun addDismissableOverlay(view: View) {
        if (!dismissableOverlays.contains(view)) {
            dismissableOverlays.add(view)
        }
    }

    fun clearDismissableOverlays() {
        dismissableOverlays.clear()
    }

    /**
     * This Runnable handles the continuous updates for a long-press gesture.
     * It will only start firing after the `initialDelay`.
     */
    private val continuousUpdateRunnable = object : Runnable {
        override fun run() {
            // Check if the user is still holding their finger down.
            if (!isHeldDown) {
                return
            }

            // Trigger the appropriate life change. This is the first and all
            // subsequent calls for a long-press.
            if (isIncreasing) {
                onLifeIncreasedListener?.invoke()
            } else {
                onLifeDecreasedListener?.invoke()
            }

            // Accelerate the update rate for the next interval.
            currentInterval = (currentInterval - accelerationRate).coerceAtLeast(minInterval)

            // Schedule the next update.
            handler.postDelayed(this, currentInterval)
        }
    }

    /**
     * Overrides the default touch event handling to implement custom click and hold logic.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val wasOverlayVisible = dismissableOverlays.any { it.isVisible }
            if (wasOverlayVisible) {
                dismissableOverlays.forEach { it.visibility = GONE }
                return true // Consume the touch; don't change life total.
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isHeldDown = true
                isIncreasing = event.x >= width / 2

                // Reset the long-press speed for each new press.
                currentInterval = startInterval

                // Schedule the long-press runnable. It will execute only if the
                // user holds their finger down longer than `initialDelay`.
                handler.postDelayed(continuousUpdateRunnable, initialDelay)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // If this "up" event doesn't correspond to a "down" we handled, ignore it.
                if (!isHeldDown) {
                    return false
                }
                isHeldDown = false

                // Crucially, ALWAYS cancel the scheduled long-press runnable.
                handler.removeCallbacks(continuousUpdateRunnable)

                // If the user lifts their finger *before* the long-press delay has
                // passed, we interpret it as a single tap.
                if (event.eventTime - event.downTime < initialDelay) {
                    // A "click" was detected, so we call performClick()
                    // to handle it. This is the recommended practice for
                    // accessibility and consistent behavior.
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Calling super.performClick() is required to trigger standard
        // accessibility events, like playing click sounds.
        super.performClick()

        // The click action logic is moved here from onTouchEvent.
        // This will now be executed when a tap is detected.
        if (isIncreasing) {
            onLifeIncreasedListener?.invoke()
        } else {
            onLifeDecreasedListener?.invoke()
        }

        // Return true to indicate that the click event has been handled.
        return true
    }

    // The old `stopContinuousUpdates` and `performClick` methods are no longer needed
    // with this new, cleaner implementation. You can remove them if you wish.
}