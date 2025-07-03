package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player

data class PlayerDamageItem(val player: Player, val damage: Int)

class CommanderDamageAdapter(
    private val targetPlayerIndex: Int,
    private val angle: Int,
    private val onDamageIncremented: (opponentIndex: Int) -> Unit,
    private val onDamageDecremented: (opponentIndex: Int) -> Unit
) : ListAdapter<PlayerDamageItem, CommanderDamageAdapter.DamageViewHolder>(PlayerDamageDiffCallback()) {

    // Define view types for all layouts
    private val viewTypeNormal = 0
    private val viewTypeRotatedRight = 1
    private val viewTypeRotatedLeft = 2
    private val viewTypeRotated180 = 3
    private var decrementModePosition = RecyclerView.NO_POSITION

    private fun setDecrementMode(position: Int) {
        val previousPosition = decrementModePosition
        decrementModePosition = if (previousPosition == position) RecyclerView.NO_POSITION else position

        if (previousPosition != RecyclerView.NO_POSITION) notifyItemChanged(previousPosition)
        if (decrementModePosition != RecyclerView.NO_POSITION) notifyItemChanged(decrementModePosition)
    }

    private fun disableDecrementMode() {
        if (decrementModePosition != RecyclerView.NO_POSITION) {
            val positionToUpdate = decrementModePosition
            decrementModePosition = RecyclerView.NO_POSITION
            notifyItemChanged(positionToUpdate)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // Return a different view type based on the exact angle
        return when (angle) {
            90 -> viewTypeRotatedRight
            -90 -> viewTypeRotatedLeft
            180 -> viewTypeRotated180
            else -> viewTypeNormal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DamageViewHolder {
        // Inflate the correct layout based on the view type
        val layoutId = when (viewType) {
            viewTypeRotatedRight -> R.layout.item_commander_damage_rotated_right
            viewTypeRotatedLeft -> R.layout.item_commander_damage_rotated_left
            viewTypeRotated180 -> R.layout.item_commander_damage_rotated_180
            else -> R.layout.item_commander_damage
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return DamageViewHolder(view) { position, isLongClick ->
            val item = getItem(position)
            if (item.player.playerIndex != targetPlayerIndex) {
                if (isLongClick) {
                    setDecrementMode(position)
                } else {
                    // Tapping the box directly increments
                    disableDecrementMode()
                    onDamageIncremented(item.player.playerIndex)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: DamageViewHolder, position: Int) {
        holder.bind(getItem(position), position == decrementModePosition, targetPlayerIndex, onDamageDecremented)
    }

    inner class DamageViewHolder(
        itemView: View,
        private val listener: (position: Int, isLongClick: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        private val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        private val decrementButton: ImageView = itemView.findViewById(R.id.iv_decrement_button)
        private val defaultFillColor = ContextCompat.getColor(itemView.context, R.color.default_segment_background)

        init {
            damageAmount.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener(adapterPosition, false) // isLongClick = false
                }
            }
            damageAmount.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener(adapterPosition, true) // isLongClick = true
                }
                true
            }
        }

        fun bind(
            item: PlayerDamageItem,
            isDecrementMode: Boolean,
            targetPlayerIndex: Int,
            onDamageDecremented: (opponentIndex: Int) -> Unit
        ) {
            // If this is the placeholder item, clear its text, make it invisible, and stop.
            if (item.player.playerIndex == -1) {
                opponentName.text = ""
                damageAmount.text = ""
                itemView.visibility = View.INVISIBLE
                return
            }
            // Otherwise, ensure the view is visible and bind the player data.
            itemView.visibility = View.VISIBLE
            opponentName.text = item.player.name
            val background = damageAmount.background as GradientDrawable

            // Rotate the text content to match the segment's orientation
            opponentName.rotation = angle.toFloat()
            damageAmount.rotation = angle.toFloat()

            item.player.color?.let {
                val color = it.toColorInt()
                val darkerFillColor = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
                background.setColor(darkerFillColor)
            } ?: run {
                background.setColor(defaultFillColor)
            }

            if (item.player.playerIndex == targetPlayerIndex) {
                damageAmount.text = itemView.context.getString(R.string.me)
                itemView.alpha = 0.6f
                damageAmount.isClickable = false
                decrementButton.visibility = View.GONE
            } else {
                damageAmount.text = item.damage.toString()
                itemView.alpha = 1.0f
                damageAmount.isClickable = true
                decrementButton.visibility = if (isDecrementMode) View.VISIBLE else View.GONE

                decrementButton.setOnClickListener {
                    onDamageDecremented(item.player.playerIndex)
                }
            }
        }
    }
}

class PlayerDamageDiffCallback : DiffUtil.ItemCallback<PlayerDamageItem>() {
    override fun areItemsTheSame(oldItem: PlayerDamageItem, newItem: PlayerDamageItem): Boolean {
        return oldItem.player.playerIndex == newItem.player.playerIndex
    }

    override fun areContentsTheSame(oldItem: PlayerDamageItem, newItem: PlayerDamageItem): Boolean {
        return oldItem == newItem
    }
}