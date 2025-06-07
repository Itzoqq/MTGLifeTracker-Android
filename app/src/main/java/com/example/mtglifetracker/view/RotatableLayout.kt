package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.withSave
import com.example.mtglifetracker.R

/**
 * A robust FrameLayout that correctly handles measurement, drawing, and touch events for a rotated view.
 * It acts as a compound view, inflating its own content and exposing its children as properties.
 */
class RotatableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Public properties to expose the inner views to the MainActivity.
    val lifeCounter: LifeCounterView
    val deltaCounter: TextView

    internal var angle: Int = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotatableLayout,
            0, 0
        ).apply {
            try {
                angle = getInteger(R.styleable.RotatableLayout_angle, 0)
            } finally {
                recycle()
            }
        }

        rotation = 0f
        setWillNotDraw(false)

        // Inflate the player segment layout and attach it as a child of this FrameLayout.
        inflate(context, R.layout.layout_player_segment, this)

        // Find the views within the inflated layout and assign them to the public properties.
        lifeCounter = findViewById(R.id.lifeCounter)
        deltaCounter = findViewById(R.id.deltaCounter)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isRotated90Degrees()) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withSave {
            when (angle) {
                90 -> {
                    translate(width.toFloat(), 0f)
                    rotate(90f)
                }
                180 -> {
                    rotate(180f, width / 2f, height / 2f)
                }
                270, -90 -> {
                    translate(0f, height.toFloat())
                    rotate(270f)
                }
            }
            super.dispatchDraw(this)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val matrix = Matrix()
        when (angle) {
            90 -> {
                matrix.setRotate(-90f)
                matrix.postTranslate(0f, width.toFloat())
            }
            180 -> {
                matrix.setRotate(-180f)
                matrix.postTranslate(width.toFloat(), height.toFloat())
            }
            270, -90 -> {
                matrix.setRotate(-270f)
                matrix.postTranslate(height.toFloat(), 0f)
            }
        }

        event.transform(matrix)
        val handled = super.dispatchTouchEvent(event)
        return handled
    }

    private fun isRotated90Degrees(): Boolean {
        return angle % 180 != 0
    }
}