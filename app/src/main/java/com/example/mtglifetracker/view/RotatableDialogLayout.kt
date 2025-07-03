package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.withSave

/**
 * A simple FrameLayout that correctly measures and draws its children with a specified rotation.
 * This is crucial for containing complex views like a RecyclerView with a GridLayoutManager.
 */
class RotatableDialogLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var angle: Int = 0
        set(value) {
            field = value
            // Trigger a remeasure and redraw when the angle is set.
            requestLayout()
            invalidate()
        }

    init {
        // This is required for dispatchDraw to be called on a layout.
        setWillNotDraw(false)
    }

    private fun isRotated90degrees(): Boolean {
        return angle % 180 != 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // When rotated by 90 or 270, the width and height effectively swap.
        // We must swap the measure specs to ensure the children are measured correctly.
        if (isRotated90degrees()) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withSave {
            // Apply the rotation transform to the canvas before drawing children.
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
}