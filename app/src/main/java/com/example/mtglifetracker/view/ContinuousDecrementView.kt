package com.example.mtglifetracker.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class ContinuousDecrementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    var onDecrementListener: (() -> Unit)? = null

    private var isHeldDown = false
    private val viewHandler = Handler(Looper.getMainLooper())

    // Slower timing constants
    private val initialDelay = 500L
    private val startInterval = 250L
    private val minInterval = 100L
    private val accelerationRate = 15L
    private var currentInterval = startInterval

    private val continuousUpdateRunnable = object : Runnable {
        override fun run() {
            if (!isHeldDown) return
            onDecrementListener?.invoke()
            currentInterval = (currentInterval - accelerationRate).coerceAtLeast(minInterval)
            viewHandler.postDelayed(this, currentInterval)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isHeldDown = true
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
        onDecrementListener?.invoke()
        return true
    }
}