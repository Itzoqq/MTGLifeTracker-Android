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
    private val onDamageIncremented: (opponentIndex: Int) -> Unit,
    private val onDamageDecremented: (opponentIndex: Int) -> Unit
) : ListAdapter<PlayerDamageItem, CommanderDamageAdapter.DamageViewHolder>(PlayerDamageDiffCallback()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DamageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_commander_damage, parent, false)
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

    class DamageViewHolder(
        itemView: View,
        private val listener: (position: Int, isLongClick: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        private val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        private val decrementButton: ImageView = itemView.findViewById(R.id.iv_decrement_button)
        private val defaultFillColor = ContextCompat.getColor(itemView.context, R.color.default_segment_background)

        init {
            damageAmount.setOnClickListener {
                // Use adapterPosition, the working predecessor to bindingAdapterPosition
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
            opponentName.text = item.player.name
            val background = damageAmount.background as GradientDrawable

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
                    // The only role of the minus button is to decrement
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