package com.example.mtglifetracker.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.example.mtglifetracker.util.Logger

/**
 * A custom [AppCompatImageView] that triggers a continuous, accelerating callback
 * when held down by the user.
 *
 * This view is designed for UI elements like a "minus" button where a user might want to
 * press and hold to decrement a value rapidly. It has an initial delay before the
 * repeating action starts, and the interval between actions decreases over time,
 * creating an acceleration effect. A single, short tap will trigger a normal `performClick`.
 *
 * @property onDecrementListener A lambda function that is invoked for each decrement action.
 */
class ContinuousDecrementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    // A lambda that will be called each time a decrement event is fired.
    var onDecrementListener: (() -> Unit)? = null

    // A flag to track if the view is currently being held down.
    private var isHeldDown = false
    // A handler to post delayed runnables on the main UI thread.
    private val viewHandler = Handler(Looper.getMainLooper())

    // --- Timing Constants for the continuous press action ---
    private val initialDelay = 500L      // 500ms delay before the continuous decrementing starts.
    private val startInterval = 250L     // The interval between the first few decrements.
    private val minInterval = 100L       // The fastest possible interval to prevent it from being too fast.
    private val accelerationRate = 15L   // How much to decrease the interval by on each tick (in ms).
    private var currentInterval = startInterval

    /**
     * A [Runnable] that contains the logic for the repeating action.
     * When executed, it invokes the listener and schedules itself to run again
     * after a shorter delay, creating the acceleration effect.
     */
    private val continuousUpdateRunnable = object : Runnable {
        override fun run() {
            // Do nothing if the user has lifted their finger.
            if (!isHeldDown) {
                Logger.d("ContinuousDecrementView: Runnable executed but view is no longer held down. Stopping.")
                return
            }

            // Invoke the listener to perform the actual decrement action.
            Logger.d("ContinuousDecrementView: Continuous update fired. Invoking listener.")
            onDecrementListener?.invoke()

            // Decrease the interval for the next execution, respecting the minimum interval.
            currentInterval = (currentInterval - accelerationRate).coerceAtLeast(minInterval)
            Logger.d("ContinuousDecrementView: New interval will be $currentInterval ms.")

            // Schedule the next execution.
            viewHandler.postDelayed(this, currentInterval)
        }
    }

    /**
     * Handles touch events to manage the continuous press logic.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Logger.d("ContinuousDecrementView: ACTION_DOWN event.")
                isHeldDown = true
                // Reset the interval to its starting value for each new press.
                currentInterval = startInterval
                // Post the runnable to be executed after the initial delay.
                Logger.d("ContinuousDecrementView: Scheduling continuous runnable with initial delay of $initialDelay ms.")
                viewHandler.postDelayed(continuousUpdateRunnable, initialDelay)
                return true // Return true to indicate we have handled this event.
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Logger.d("ContinuousDecrementView: ACTION_UP or ACTION_CANCEL event.")
                // If the view wasn't being held, there's nothing to do.
                if (!isHeldDown) return false

                isHeldDown = false
                // Remove any pending executions of the runnable from the handler's queue.
                Logger.d("ContinuousDecrementView: Removing continuous runnable from handler.")
                viewHandler.removeCallbacks(continuousUpdateRunnable)

                // If the press duration was shorter than the initial delay, it's a single tap.
                if (event.eventTime - event.downTime < initialDelay) {
                    Logger.d("ContinuousDecrementView: Press was shorter than delay. Performing single click.")
                    performClick()
                } else {
                    Logger.d("ContinuousDecrementView: Press was longer than delay. Continuous action is finished.")
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Performs a single click action.
     * This is called for short taps and also provides accessibility support.
     */
    override fun performClick(): Boolean {
        super.performClick()
        Logger.d("ContinuousDecrementView: performClick called. Invoking listener once.")
        onDecrementListener?.invoke()
        return true
    }
}