package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.graphics.withSave
import com.example.mtglifetracker.R

/**
 * A custom FrameLayout that correctly handles measurement and drawing for a rotated view.
 * It also transforms incoming touch events so that child views receive coordinates relative
 * to the rotated orientation, making their internal logic much simpler.
 */
class RotatableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** A public property to expose the inner LifeCounterView to the MainActivity. */
    val lifeCounter: LifeCounterView
    private var angle: Int = 0

    // A reusable matrix for transforming touch events.
    private val motionEventMatrix = Matrix()

    init {
        // Read the value from our custom 'angle' attribute in the XML.
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

        // Handle all rotation manually, so neutralize the view's built-in property.
        rotation = 0f
        // A custom view that draws on its own must disable this flag for dispatchDraw to be called.
        setWillNotDraw(false)

        // Inflate the player segment layout and attach it as a child of this FrameLayout.
        inflate(context, R.layout.layout_player_segment, this)
        lifeCounter = findViewById(R.id.lifeCounter)
    }

    /**
     * Overridden to swap the width and height measurement specs when the view is rotated
     * by 90 or 270 degrees, ensuring children are measured for the rotated space.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isRotated90Degrees()) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * Overridden to manually transform the canvas before any children are drawn.
     * This ensures the entire view content is drawn with the correct rotation.
     */
    override fun dispatchDraw(canvas: Canvas) {
        // Use the KTX 'withSave' to automatically handle canvas.save() and canvas.restore().
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
            // After the canvas is transformed, draw the children onto it.
            super.dispatchDraw(this)
        }
    }

    /**
     * Overridden to intercept touch events, transform their coordinates to match the
     * view's rotation, and then dispatch the transformed event to child views.
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // Prepare the transformation matrix to reverse the rotation.
        motionEventMatrix.reset()
        when (angle) {
            90 -> {
                motionEventMatrix.setRotate(-90f)
                motionEventMatrix.postTranslate(0f, width.toFloat())
            }
            180 -> {
                motionEventMatrix.setRotate(-180f)
                motionEventMatrix.postTranslate(width.toFloat(), height.toFloat())
            }
            270, -90 -> {
                motionEventMatrix.setRotate(-270f)
                motionEventMatrix.postTranslate(height.toFloat(), 0f)
            }
        }

        // Create a copy of the motion event and apply the inverse transformation.
        val transformedEvent = MotionEvent.obtain(event)
        transformedEvent.transform(motionEventMatrix)

        // Dispatch the new, transformed event to the children (e.g., LifeCounterView).
        val handled = super.dispatchTouchEvent(transformedEvent)

        // Recycle the copied event to avoid memory leaks.
        transformedEvent.recycle()

        return handled
    }

    /**
     * Checks if the view's angle is a right-angle rotation.
     */
    private fun isRotated90Degrees(): Boolean {
        return angle % 180 != 0
    }
}