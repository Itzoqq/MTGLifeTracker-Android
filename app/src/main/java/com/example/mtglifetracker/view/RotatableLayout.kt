package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.mtglifetracker.R

class RotatableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val lifeCounter: LifeCounterView
    private var angle: Int = 0

    init {
        // Read the value from our custom 'angle' attribute in the XML
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

        // IMPORTANT: Neutralize the view's built-in rotation property to prevent double rotation.
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
        canvas.save()

        // Use our custom 'angle' property for all transformations
        when (angle) {
            90 -> {
                canvas.translate(width.toFloat(), 0f)
                canvas.rotate(90f)
            }
            180 -> {
                canvas.rotate(180f, width / 2f, height / 2f)
            }
            270, -90 -> {
                canvas.translate(0f, height.toFloat())
                canvas.rotate(270f)
            }
        }

        super.dispatchDraw(canvas)
        canvas.restore()
    }

    private fun isRotated90Degrees(): Boolean {
        // Use our custom 'angle' property here as well
        return angle % 180 != 0
    }
}