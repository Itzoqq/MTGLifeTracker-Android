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
import androidx.core.view.isVisible

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
        return DamageViewHolder(view)
    }

    override fun onBindViewHolder(holder: DamageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DamageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        private val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        private val decrementButton: ImageView = itemView.findViewById(R.id.iv_decrement_button)
        private val defaultFillColor = ContextCompat.getColor(itemView.context, R.color.default_segment_background)

        fun bind(item: PlayerDamageItem) {
            if (item.player.playerIndex == -1) {
                opponentName.text = ""
                damageAmount.text = ""
                itemView.visibility = View.INVISIBLE
                return
            }

            itemView.visibility = View.VISIBLE
            opponentName.text = item.player.name
            val background = damageAmount.background as GradientDrawable

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
                damageAmount.setOnLongClickListener(null)
                decrementButton.visibility = View.GONE
            } else {
                damageAmount.text = item.damage.toString()
                itemView.alpha = 1.0f
                damageAmount.isClickable = true

                damageAmount.setOnClickListener {
                    decrementButton.visibility = View.GONE
                    onDamageIncremented(item.player.playerIndex)
                }

                damageAmount.setOnLongClickListener {
                    decrementButton.visibility = if (decrementButton.isVisible) View.GONE else View.VISIBLE
                    true
                }

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