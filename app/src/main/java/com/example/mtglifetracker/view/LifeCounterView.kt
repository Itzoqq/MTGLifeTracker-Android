package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger

/**
 * A custom [AppCompatTextView] designed specifically for displaying and interacting with a player's life total.
 *
 * This view has several key features:
 * 1.  **Tap and Hold Interaction:** A single tap on the left/right half changes life by 1. Pressing and
 * holding will continuously change the life total with an accelerating rate.
 * 2.  **Life Delta Animation:** When life changes, a small text indicator appears next to the main
 * total, showing the accumulated change (e.g., "+3" or "-5"). This indicator fades after a delay.
 * 3.  **Overlay Dismissal:** A tap on this view will dismiss any registered overlay views (like popups),
 * providing an intuitive way for users to close menus.
 */
class LifeCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // --- Properties for Life Delta Animation ---
    private var displayedDelta: Int = 0
    private var accumulatedDelta: Int = 0
    private var isDeltaVisible: Boolean = false
    private val deltaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }
    private val mainTextBounds = Rect() // Pre-allocated Rect to avoid object creation during onDraw.
    private val viewHandler = Handler(Looper.getMainLooper())
    private val endDeltaSequenceRunnable = Runnable {
        Logger.d("LifeCounterView: Delta sequence timer finished. Hiding delta.")
        isDeltaVisible = false
        accumulatedDelta = 0
        invalidate() // Trigger a redraw to remove the delta text.
    }

    /**
     * Sets the life total with a smooth animation for the main number.
     * This is used for mass updates like game resets to avoid showing a large delta.
     *
     * @param newLife The new life total to display.
     */
    fun setLifeAnimate(newLife: Int) {
        Logger.d("LifeCounterView: setLifeAnimate called with new life: $newLife.")
        text = newLife.toString()
        accumulatedDelta = 0
        if (isDeltaVisible) {
            isDeltaVisible = false
            invalidate()
        }
    }

    /**
     * Gets or sets the current life total.
     *
     * When setting the life, it calculates the difference from the current life, updates
     * the delta display, and resets the timer for hiding the delta.
     */
    var life: Int
        get() = text.toString().toIntOrNull() ?: 0
        set(value) {
            val singleStepDelta = value - life
            Logger.d("LifeCounterView: Life set. Old value=$life, New value=$value, Step delta=$singleStepDelta.")

            // Only update the delta and reset the timer if the life value has actually changed.
            if (singleStepDelta != 0) {
                accumulatedDelta += singleStepDelta
                Logger.d("LifeCounterView: Accumulated delta is now $accumulatedDelta.")
                displayDelta(accumulatedDelta)

                // Remove any pending tasks to hide the delta and post a new one.
                viewHandler.removeCallbacks(endDeltaSequenceRunnable)
                viewHandler.postDelayed(endDeltaSequenceRunnable, 3000)
                Logger.d("LifeCounterView: Reset delta visibility timer for 3000ms.")
            }
            // Always update the main text to display the correct life total.
            text = value.toString()
        }

    /**
     * Prepares the delta text for drawing and triggers a redraw.
     * @param delta The accumulated life change to display.
     */
    private fun displayDelta(delta: Int) {
        Logger.d("LifeCounterView: displayDelta called with delta: $delta.")
        displayedDelta = delta
        isDeltaVisible = true
        // Set the delta text color based on whether the change is positive or negative.
        deltaPaint.color = when {
            delta > 0 -> context.getColor(R.color.delta_positive)
            delta < 0 -> context.getColor(R.color.delta_negative)
            else -> context.getColor(R.color.white) // Should not happen, but a safe default.
        }
        invalidate() // Request a redraw to show the delta.
    }

    /**
     * Overridden to draw the custom life delta text next to the main life total.
     */
    override fun onDraw(canvas: Canvas) {
        // First, draw the main life total text as normal.
        super.onDraw(canvas)

        // If the delta is visible, draw it on the canvas.
        if (isDeltaVisible) {
            Logger.d("LifeCounterView: onDraw is drawing the delta text '$displayedDelta'.")
            val deltaText = if (displayedDelta > 0) "+$displayedDelta" else displayedDelta.toString()
            val mainText = text.toString()

            deltaPaint.textSize = this.textSize * 0.4f

            // Calculate the position of the delta text relative to the main life total.
            paint.getTextBounds(mainText, 0, mainText.length, mainTextBounds)
            val mainTextRightEdge = (width / 2f) + (mainTextBounds.width() / 2f)
            val mainTextTopEdge = (height / 2f) - (mainTextBounds.height() / 2f)
            val deltaX = mainTextRightEdge + (this.textSize * 0.1f)
            val verticalOffset = this.textSize * 0.2f
            val deltaYBaseline = mainTextTopEdge - deltaPaint.ascent() - verticalOffset

            canvas.drawText(deltaText, deltaX, deltaYBaseline, deltaPaint)
        }
    }

    // --- Click/Hold Logic and Overlay Dismissal ---
    var onLifeIncreasedListener: (() -> Unit)? = null
    var onLifeDecreasedListener: (() -> Unit)? = null

    private var isHeldDown = false
    private var isIncreasing = false // Tracks which half of the view was pressed.
    private val dismissibleOverlays = mutableListOf<View>()

    // Timing constants for press-and-hold interaction.
    private val initialDelay = 400L
    private val startInterval = 150L
    private val minInterval = 50L
    private val accelerationRate = 10L
    private var currentInterval = startInterval

    /**
     * Registers a view that should be dismissed when this LifeCounterView is tapped.
     * @param view The overlay view (e.g., a popup) to be dismissed.
     */
    fun addDismissibleOverlay(view: View) {
        if (!dismissibleOverlays.contains(view)) {
            Logger.d("LifeCounterView: Adding dismissible overlay: ${view.javaClass.simpleName}")
            dismissibleOverlays.add(view)
        }
    }

    /**
     * A [Runnable] for the continuous press-and-hold action.
     */
    private val continuousUpdateRunnable = object : Runnable {
        override fun run() {
            if (!isHeldDown) return
            Logger.d("LifeCounterView: Continuous update fired. isIncreasing=$isIncreasing")
            if (isIncreasing) {
                onLifeIncreasedListener?.invoke()
            } else {
                onLifeDecreasedListener?.invoke()
            }
            // Accelerate the next update.
            currentInterval = (currentInterval - accelerationRate).coerceAtLeast(minInterval)
            viewHandler.postDelayed(this, currentInterval)
        }
    }

    /**
     * Handles all touch events for the view.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // First, check if a tap should dismiss an overlay.
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (dismissibleOverlays.any { it.isVisible }) {
                Logger.i("LifeCounterView: Overlay is visible. Dismissing overlays instead of changing life.")
                dismissibleOverlays.forEach { it.visibility = GONE }
                return true // Consume the event to prevent life change.
            }
        }

        // Standard touch handling for life changes.
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Logger.d("LifeCounterView: ACTION_DOWN event.")
                isHeldDown = true
                isIncreasing = event.x >= width / 2 // Determine if press is on left or right half.
                currentInterval = startInterval
                Logger.d("LifeCounterView: Press registered. isIncreasing=$isIncreasing. Scheduling continuous runnable.")
                viewHandler.postDelayed(continuousUpdateRunnable, initialDelay)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Logger.d("LifeCounterView: ACTION_UP or ACTION_CANCEL event.")
                if (!isHeldDown) return false
                isHeldDown = false
                viewHandler.removeCallbacks(continuousUpdateRunnable)
                // If the press was very short, treat it as a single tap.
                if (event.eventTime - event.downTime < initialDelay) {
                    Logger.d("LifeCounterView: Short press detected. Performing single click.")
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Performs a single click action, used for short taps and accessibility.
     */
    override fun performClick(): Boolean {
        super.performClick()
        Logger.d("LifeCounterView: performClick called. isIncreasing=$isIncreasing")
        if (isIncreasing) {
            onLifeIncreasedListener?.invoke()
        } else {
            onLifeDecreasedListener?.invoke()
        }
        return true
    }
}