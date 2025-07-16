package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import com.example.mtglifetracker.util.Logger

/**
 * A custom [View] intended to serve as a container or display area for various
 * player-specific counters (e.g., poison, energy).
 *
 * Currently, this view acts as a simple placeholder, drawing a solid colored
 * background. It is designed to be built upon as more counter-tracking features
 * are added to the application.
 */
class PlayerCountersView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // A Paint object used to draw the background color of the view.
    private val paint = Paint().apply {
        color = "#424242".toColorInt() // A lighter shade of gray
        style = Paint.Style.FILL
    }

    /**
     * Called when the view should render its content.
     * This method is responsible for all custom drawing within the view's bounds.
     *
     * @param canvas The [Canvas] on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Logger.d("PlayerCountersView: onDraw called. Drawing background rect.")
        // Draw a solid rectangle that fills the entire area of the view.
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}