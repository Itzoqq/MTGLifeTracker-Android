package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class VerticalTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    // Determines if text reads top-to-bottom (90 deg) or bottom-to-top (-90 deg)
    var isTopDown = false

    init {
        // Center the text within its bounds before rotating
        gravity = Gravity.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Swap the width and height specs before passing them to the superclass
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        // Swap the final measured dimensions
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState

        canvas.save()

        // Rotate the canvas to make the text draw vertically
        if (isTopDown) {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(90f)
        } else {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(-90f)
        }

        // Move the drawing position to account for padding
        canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
        // Ask the layout to draw itself on the now-rotated canvas
        layout?.draw(canvas)
        canvas.restore()
    }
}