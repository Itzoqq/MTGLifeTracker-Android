package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * A custom ItemDecoration that adds a divider between items in a RecyclerView,
 * but crucially, does NOT draw a divider after the last item in the list.
 *
 * @param context The context for retrieving the drawable.
 * @param resId The resource ID of the divider drawable.
 */
class DividerItemDecorationExceptLast(context: Context, resId: Int) : RecyclerView.ItemDecoration() {

    private val divider: Drawable? = ContextCompat.getDrawable(context, resId)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        // This loop draws a divider BELOW each item, but stops before the last one.
        // The "- 1" is the key to skipping the final item.
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + (divider?.intrinsicHeight ?: 0)

            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
        }
    }
}