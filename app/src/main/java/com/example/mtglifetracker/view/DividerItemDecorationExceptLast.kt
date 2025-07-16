package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.util.Logger

/**
 * A custom [RecyclerView.ItemDecoration] that adds a divider between items,
 * but crucially, does NOT draw a divider after the very last item in the list.
 *
 * This is a common UI requirement to avoid having an unnecessary separator at the
 * end of a list, making the layout look cleaner.
 *
 * @param context The context needed to retrieve the drawable resource.
 * @param resId The resource ID of the divider drawable to be used.
 */
class DividerItemDecorationExceptLast(context: Context, resId: Int) : RecyclerView.ItemDecoration() {

    // The drawable to be used as a divider. It's nullable in case the resource ID is invalid.
    private val divider: Drawable? = ContextCompat.getDrawable(context, resId)

    /**
     * Overridden to draw the dividers on the RecyclerView's canvas.
     *
     * This method is called by the RecyclerView during its drawing cycle. It iterates
     * through the visible children of the RecyclerView and draws the divider drawable
     * below each one, except for the last child.
     *
     * @param c The [Canvas] to draw on.
     * @param parent The [RecyclerView] being decorated.
     * @param state The current state of the RecyclerView.
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        Logger.d("DividerItemDecoration: onDraw called for RecyclerView.")
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        // This loop draws a divider BELOW each item.
        // The "- 1" is the key to skipping the final item in the list, which is
        // the entire purpose of this custom class.
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            Logger.d("DividerItemDecoration: Drawing divider below child at index $i.")

            // Calculate the top and bottom coordinates for the divider.
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + (divider?.intrinsicHeight ?: 0)

            // Set the bounds and draw the divider.
            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
        }
    }
}