package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.example.mtglifetracker.util.Logger
import androidx.core.graphics.withSave

/**
 * A custom [AppCompatTextView] that draws its text vertically.
 *
 * This view is essential for layouts that require text to be rotated, such as the titles
 * in the commander damage dialogs for side-oriented players. It achieves this effect by
 * swapping its measured dimensions when rotated and by applying a rotation transform
 * to the canvas before drawing its text content.
 *
 * @property isTopDown A boolean that controls the direction of the text. If `true`, the text
 * reads from top to bottom (a 90-degree rotation). If `false`, it reads from bottom to top
 * (a -90 or 270-degree rotation).
 */
class VerticalTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    var isTopDown = false

    init {
        // Center the text within the view's bounds before any rotation is applied.
        // This ensures the text is properly aligned when drawn on the rotated canvas.
        gravity = Gravity.CENTER
    }

    /**
     * Overridden to correctly measure the view in a rotated context.
     *
     * When text is drawn vertically, the concepts of width and height are swapped.
     * This method swaps the incoming `widthMeasureSpec` and `heightMeasureSpec` before
     * passing them to the superclass. It then swaps the final measured dimensions back
     * to ensure the layout system correctly allocates space for the rotated view.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Logger.d("VerticalTextView: onMeasure called. Swapping measure specs.")
        // Swap the specs to measure the view as if it were horizontal.
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        // Swap the final dimensions back to their correct orientation.
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    /**
     * Overridden to apply the rotation to the canvas before drawing the text.
     *
     * This is the core of the vertical drawing logic. Instead of rotating the view
     * itself (which can complicate layout), we rotate the canvas and then let the
     * standard text drawing mechanism do its work on the rotated surface.
     */
    override fun onDraw(canvas: Canvas) {
        Logger.d("VerticalTextView: onDraw called. isTopDown=$isTopDown.")
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState

        // Save the canvas's current state so we can restore it later.
        canvas.withSave {

            // Apply the correct rotation and translation based on the desired text direction.
            if (isTopDown) {
                // For top-to-bottom text: translate to the top-right corner, then rotate 90 degrees clockwise.
                translate(width.toFloat(), 0f)
                rotate(90f)
            } else {
                // For bottom-to-top text: translate to the bottom-left corner, then rotate 90 degrees counter-clockwise.
                translate(0f, height.toFloat())
                rotate(-90f)
            }

            // Move the drawing position to account for any padding set on the view.
            translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())

            // Ask the view's layout to draw itself on the now-rotated canvas.
            layout?.draw(this)

            // Restore the canvas to its original, non-rotated state.
        }
    }
}