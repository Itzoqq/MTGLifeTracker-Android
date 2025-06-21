package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R

class ColorAdapter(
    private val colors: List<String>,
    private val onColorSelected: (String?) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    fun setSelectedColor(color: String?) {
        val index = colors.indexOf(color)
        val oldPosition = selectedPosition
        selectedPosition = if (index != -1) index else RecyclerView.NO_POSITION

        // Notify both the old and new items to redraw
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color_swatch, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val oldSelected = selectedPosition
                if (adapterPosition == selectedPosition) {
                    // Deselect if clicking the same item
                    selectedPosition = RecyclerView.NO_POSITION
                    onColorSelected(null)
                } else {
                    selectedPosition = adapterPosition
                    onColorSelected(colors[selectedPosition])
                }

                // Redraw the old and new selected items
                if (oldSelected != RecyclerView.NO_POSITION) {
                    notifyItemChanged(oldSelected)
                }
                notifyItemChanged(selectedPosition)
            }
        }

        fun bind(colorString: String, isSelected: Boolean) {
            // --- THIS IS THE FIX ---
            // Create the drawable programmatically to avoid the ClassCastException
            val newDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(colorString.toColorInt())

                val strokeWidth = if (isSelected) {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.selected_stroke_width)
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.default_stroke_width)
                }

                val strokeColor = if (isSelected) Color.WHITE else Color.GRAY

                setStroke(strokeWidth, strokeColor)
            }
            itemView.background = newDrawable
            // --- END OF FIX ---
        }
    }
}