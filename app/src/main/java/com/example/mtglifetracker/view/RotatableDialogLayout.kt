package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.withSave
import com.example.mtglifetracker.util.Logger

/**
 * A custom [FrameLayout] that correctly measures and draws its children with a specified rotation.
 *
 * This layout is crucial for containing complex views (like a RecyclerView with a GridLayoutManager)
 * that need to be displayed at a 90, 180, or 270-degree angle. It overrides `onMeasure` to
 * correctly handle swapped width and height for 90/270 degree rotations, and it overrides
 * `dispatchDraw` to apply the rotation transform to the canvas before any children are drawn.
 */
class RotatableDialogLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * The rotation angle for the layout's content. Can be 0, 90, 180, or 270/-90.
     * When this value is set, it automatically triggers a re-measure and re-draw of the layout.
     */
    var angle: Int = 0
        set(value) {
            if (field != value) {
                Logger.i("RotatableDialogLayout: Angle set from $field to $value.")
                field = value
                // Trigger a full re-layout pass to account for the new orientation.
                requestLayout()
                invalidate()
            }
        }

    init {
        // This is required for a ViewGroup to have its dispatchDraw or onDraw methods called.
        // By default, ViewGroups do not draw anything themselves and are optimized to skip this step.
        setWillNotDraw(false)
    }

    /**
     * Helper function to determine if the rotation is 90 or 270 degrees.
     */
    private fun isRotated90degrees(): Boolean {
        return angle % 180 != 0
    }

    /**
     * Overridden to correctly measure children within a rotated space.
     *
     * When a view is rotated by 90 or 270 degrees, its conceptual width and height are swapped.
     * We must swap the incoming `widthMeasureSpec` and `heightMeasureSpec` before passing them
     * to the superclass to ensure the children are measured with the correct constraints.
     * We then swap the final measured dimensions back.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Logger.d("RotatableDialogLayout: onMeasure called with angle $angle.")
        // When rotated by 90 or 270, the width and height effectively swap.
        if (isRotated90degrees()) {
            Logger.d("RotatableDialogLayout: Is rotated 90 degrees. Swapping measure specs.")
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * Overridden to apply the rotation transform to the canvas before children are drawn.
     *
     * This method intercepts the drawing process. It saves the canvas's current state,
     * applies the necessary rotation and translation, and then calls `super.dispatchDraw()`
     * to let the children draw themselves onto the transformed canvas. Finally, it restores
     * the canvas to its original state.
     */
    override fun dispatchDraw(canvas: Canvas) {
        // withSave is a helpful KTX extension that automatically saves and restores the canvas.
        canvas.withSave {
            Logger.d("RotatableDialogLayout: dispatchDraw called. Applying rotation of $angle degrees.")
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
            // Now that the canvas is rotated, let the standard drawing process for children occur.
            super.dispatchDraw(this)
        }
    }
}