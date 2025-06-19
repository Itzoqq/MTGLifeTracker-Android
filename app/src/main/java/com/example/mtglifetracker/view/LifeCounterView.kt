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

class LifeCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var displayedDelta: Int = 0
    private var accumulatedDelta: Int = 0
    private var isDeltaVisible: Boolean = false
    private val deltaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }

    // --- THIS IS THE FIX ---
    // Pre-allocate the Rect object here as a class property instead of inside onDraw.
    private val mainTextBounds = Rect()
    // --- END OF FIX ---

    private val viewHandler = Handler(Looper.getMainLooper())

    private val endDeltaSequenceRunnable = Runnable {
        isDeltaVisible = false
        accumulatedDelta = 0
        invalidate()
    }

    fun setLifeAnimate(newLife: Int) {
        text = newLife.toString()
        accumulatedDelta = 0
        if (isDeltaVisible) {
            isDeltaVisible = false
            invalidate()
        }
    }

    var life: Int
        get() = text.toString().toIntOrNull() ?: 0
        set(value) {
            val singleStepDelta = value - life
            if (singleStepDelta != 0 || isDeltaVisible) {
                accumulatedDelta += singleStepDelta
                displayDelta(accumulatedDelta)
                viewHandler.removeCallbacks(endDeltaSequenceRunnable)
                viewHandler.postDelayed(endDeltaSequenceRunnable, 3000)
            }
            text = value.toString()
        }

    private fun displayDelta(delta: Int) {
        displayedDelta = delta
        isDeltaVisible = true
        deltaPaint.color = when {
            delta > 0 -> context.getColor(R.color.delta_positive)
            delta < 0 -> context.getColor(R.color.delta_negative)
            else -> context.getColor(R.color.white)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isDeltaVisible) {
            val deltaText = if (displayedDelta > 0) "+$displayedDelta" else displayedDelta.toString()
            val mainText = text.toString()

            deltaPaint.textSize = this.textSize * 0.4f

            // The local allocation "val mainTextBounds = Rect()" is removed from here.
            // We now use the pre-allocated class property to store the text bounds.
            paint.getTextBounds(mainText, 0, mainText.length, mainTextBounds)

            val mainTextRightEdge = (width / 2f) + (mainTextBounds.width() / 2f)
            val mainTextTopEdge = (height / 2f) - (mainTextBounds.height() / 2f)

            val deltaX = mainTextRightEdge + (this.textSize * 0.1f)

            val verticalOffset = this.textSize * 0.2f
            val deltaYBaseline = mainTextTopEdge - deltaPaint.ascent() - verticalOffset

            canvas.drawText(deltaText, deltaX, deltaYBaseline, deltaPaint)
        }
    }

    // --- Click/Hold Logic (Unchanged) ---
    var onLifeIncreasedListener: (() -> Unit)? = null
    var onLifeDecreasedListener: (() -> Unit)? = null

    private var isHeldDown = false
    private var isIncreasing = false
    private val dismissableOverlays = mutableListOf<View>()

    private val initialDelay = 400L
    private val startInterval = 150L
    private val minInterval = 50L
    private val accelerationRate = 10L
    private var currentInterval = startInterval

    fun addDismissableOverlay(view: View) {
        if (!dismissableOverlays.contains(view)) {
            dismissableOverlays.add(view)
        }
    }

    private val continuousUpdateRunnable = object : Runnable {
        override fun run() {
            if (!isHeldDown) return
            if (isIncreasing) {
                onLifeIncreasedListener?.invoke()
            } else {
                onLifeDecreasedListener?.invoke()
            }
            currentInterval = (currentInterval - accelerationRate).coerceAtLeast(minInterval)
            viewHandler.postDelayed(this, currentInterval)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (dismissableOverlays.any { it.isVisible }) {
                dismissableOverlays.forEach { it.visibility = GONE }
                return true
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isHeldDown = true
                isIncreasing = event.x >= width / 2
                currentInterval = startInterval
                viewHandler.postDelayed(continuousUpdateRunnable, initialDelay)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isHeldDown) return false
                isHeldDown = false
                viewHandler.removeCallbacks(continuousUpdateRunnable)
                if (event.eventTime - event.downTime < initialDelay) {
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (isIncreasing) {
            onLifeIncreasedListener?.invoke()
        } else {
            onLifeDecreasedListener?.invoke()
        }
        return true
    }
}