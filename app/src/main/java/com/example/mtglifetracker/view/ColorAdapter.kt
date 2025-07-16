package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger

/**
 * A RecyclerView adapter for displaying a grid of selectable color swatches.
 *
 * This adapter is responsible for rendering a list of hex color strings as circular swatches.
 * It manages the selection state, visually highlighting the selected color with a border,
 * and notifies a listener when a color is selected or deselected.
 *
 * @param colors A list of hex color strings (e.g., "#FF0000") to be displayed.
 * @param onColorSelected A callback lambda that is invoked when a color is selected. It receives
 * the selected color string, or `null` if the current selection is cleared.
 */
class ColorAdapter(
    private val colors: List<String>,
    private val onColorSelected: (String?) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    // Holds the adapter position of the currently selected item.
    // RecyclerView.NO_POSITION indicates that nothing is selected.
    private var selectedPosition = RecyclerView.NO_POSITION

    /**
     * Programmatically sets the selected color.
     * This is used to initialize the adapter's state when editing an existing profile.
     *
     * @param color The hex string of the color to be selected, or null to clear the selection.
     */
    fun setSelectedColor(color: String?) {
        Logger.i("ColorAdapter: setSelectedColor called with color: $color")
        val index = colors.indexOf(color)
        val oldPosition = selectedPosition
        selectedPosition = if (index != -1) index else RecyclerView.NO_POSITION
        Logger.d("ColorAdapter: New selected position is $selectedPosition.")

        // Notify both the old and new items to redraw their selection states.
        if (oldPosition != RecyclerView.NO_POSITION) {
            Logger.d("ColorAdapter: Notifying item change for old position $oldPosition.")
            notifyItemChanged(oldPosition)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            Logger.d("ColorAdapter: Notifying item change for new position $selectedPosition.")
            notifyItemChanged(selectedPosition)
        }
    }

    /**
     * Called when RecyclerView needs a new [ColorViewHolder] to represent an item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        Logger.d("ColorAdapter: onCreateViewHolder called for viewType $viewType.")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color_swatch, parent, false)
        return ColorViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [ColorViewHolder] to reflect the item at the
     * given position.
     */
    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        Logger.d("ColorAdapter: onBindViewHolder called for position $position.")
        holder.bind(colors[position], position == selectedPosition)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int = colors.size

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            // Set the click listener once when the ViewHolder is created.
            itemView.setOnClickListener {
                // Ignore clicks if the position is not valid (e.g., during removal animation).
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    Logger.w("ColorAdapter: Clicked on a ViewHolder with no position. Ignoring.")
                    return@setOnClickListener
                }
                Logger.d("ColorAdapter: Clicked on position $adapterPosition.")

                val oldSelected = selectedPosition
                if (adapterPosition == selectedPosition) {
                    // If the user clicks the currently selected item, deselect it.
                    Logger.d("ColorAdapter: Deselecting position $adapterPosition.")
                    selectedPosition = RecyclerView.NO_POSITION
                    onColorSelected(null) // Notify listener that selection is cleared.
                } else {
                    // If the user clicks a new item, select it.
                    Logger.d("ColorAdapter: Selecting new position $adapterPosition.")
                    selectedPosition = adapterPosition
                    onColorSelected(colors[selectedPosition]) // Notify listener of the new color.
                }

                // Efficiently redraw only the affected items.
                if (oldSelected != RecyclerView.NO_POSITION) {
                    notifyItemChanged(oldSelected) // Redraw the previously selected item to remove its border.
                }
                notifyItemChanged(selectedPosition) // Redraw the new item to add its border.
            }
        }

        /**
         * Binds a color to this ViewHolder, setting its background and selection state.
         *
         * @param colorString The hex color string to display.
         * @param isSelected `true` if this item is the currently selected one, `false` otherwise.
         */
        fun bind(colorString: String, isSelected: Boolean) {
            Logger.d("ColorViewHolder: Binding color '$colorString' at position $adapterPosition. isSelected=$isSelected")
            // A drawable is created programmatically to avoid issues with modifying a shared
            // drawable resource, which can cause visual bugs in a RecyclerView.
            val newDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(colorString.toColorInt())

                // Determine stroke width and color based on selection state.
                val strokeWidth = if (isSelected) {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.selected_stroke_width)
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.default_stroke_width)
                }
                val strokeColor = if (isSelected) Color.WHITE else Color.GRAY

                setStroke(strokeWidth, strokeColor)
            }
            itemView.background = newDrawable
        }
    }
}