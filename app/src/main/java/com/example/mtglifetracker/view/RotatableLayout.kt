package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.graphics.withSave
import com.example.mtglifetracker.R

class RotatableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val lifeCounter: LifeCounterView
    private var angle: Int = 0

    // Matrix for transforming touch events from the parent's coordinate system
    // to this view's (rotated) local coordinate system.
    private val motionEventMatrix = Matrix()

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

        // We handle rotation manually, so set the default view rotation to 0.
        rotation = 0f
        setWillNotDraw(false)
        inflate(context, R.layout.layout_player_segment, this)
        lifeCounter = findViewById(R.id.lifeCounter)
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
        // Use the KTX 'withSave' extension to automatically handle save/restore.
        canvas.withSave {
            // Apply the correct transformation based on the custom angle.
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
        // Prepare the transformation matrix.
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

        // Create a copy of the event and apply the transformation.
        val transformedEvent = MotionEvent.obtain(event)
        transformedEvent.transform(motionEventMatrix)

        // Dispatch the transformed event to the children.
        val handled = super.dispatchTouchEvent(transformedEvent)

        // Recycle the copied event to avoid memory leaks.
        transformedEvent.recycle()

        return handled
    }

    private fun isRotated90Degrees(): Boolean {
        return angle % 180 != 0
    }
}