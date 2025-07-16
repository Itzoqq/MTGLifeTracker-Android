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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.util.Logger

/**
 * A data class to bind a player with their corresponding damage value for the adapter.
 *
 * @param player The opponent player.
 * @param damage The amount of commander damage they have dealt.
 */
data class PlayerDamageItem(val player: Player, val damage: Int)

/**
 * A [ListAdapter] for displaying the commander damage dealt by each opponent.
 *
 * This adapter is highly versatile, designed to handle multiple layout orientations
 * (normal, rotated left, rotated right, rotated 180 degrees) based on the player segment's
 * angle. It manages the display of each opponent's name and the damage they've dealt,
 * and handles user interactions for incrementing, decrementing, and showing/hiding controls.
 *
 * @param targetPlayerIndex The index of the player who is *receiving* the damage.
 * @param angle The rotation angle (0, 90, -90, 180) required for the item layouts.
 * @param onDamageIncremented A callback invoked when a user increments damage from an opponent.
 * @param onDamageDecremented A callback invoked when a user decrements damage from an opponent.
 */
class CommanderDamageAdapter(
    private val targetPlayerIndex: Int,
    private val angle: Int,
    private val onDamageIncremented: (opponentIndex: Int) -> Unit,
    private val onDamageDecremented: (opponentIndex: Int) -> Unit
) : ListAdapter<PlayerDamageItem, CommanderDamageAdapter.DamageViewHolder>(PlayerDamageDiffCallback()) {

    // Define unique integer constants for each layout type to improve readability.
    private val viewTypeNormal = 0
    private val viewTypeRotatedRight = 1
    private val viewTypeRotatedLeft = 2
    private val viewTypeRotated180 = 3

    /**
     * Determines which layout to use for an item based on the specified angle.
     * This allows the RecyclerView to correctly recycle views for different orientations.
     */
    override fun getItemViewType(position: Int): Int {
        return when (angle) {
            90 -> viewTypeRotatedRight
            -90 -> viewTypeRotatedLeft
            180 -> viewTypeRotated180
            else -> viewTypeNormal
        }
    }

    /**
     * Called when RecyclerView needs a new [DamageViewHolder].
     * It inflates the correct XML layout based on the `viewType` determined by the angle.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DamageViewHolder {
        Logger.d("CommanderDamageAdapter: onCreateViewHolder called for viewType $viewType.")
        // Select the correct layout file based on the view type (which corresponds to the angle).
        val layoutId = when (viewType) {
            viewTypeRotatedRight -> R.layout.item_commander_damage_rotated_right
            viewTypeRotatedLeft -> R.layout.item_commander_damage_rotated_left
            viewTypeRotated180 -> R.layout.item_commander_damage_rotated_180
            else -> R.layout.item_commander_damage
        }
        Logger.d("CommanderDamageAdapter: Inflating layout with ID $layoutId.")
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return DamageViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method passes the [PlayerDamageItem] to the ViewHolder's `bind` method.
     */
    override fun onBindViewHolder(holder: DamageViewHolder, position: Int) {
        Logger.d("CommanderDamageAdapter: onBindViewHolder for position $position.")
        holder.bind(getItem(position))
    }

    /**
     * A ViewHolder that describes an item view and handles its internal logic.
     * It holds references to the UI components within the item layout and sets up
     * the necessary click listeners.
     */
    inner class DamageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        // Cache view lookups for efficiency.
        private val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        private val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        private val decrementButton: ImageView = itemView.findViewById(R.id.iv_decrement_button)
        private val defaultFillColor = ContextCompat.getColor(itemView.context, R.color.default_segment_background)

        /**
         * Binds a [PlayerDamageItem] to this ViewHolder, updating the UI.
         *
         * @param item The data item containing the player and their damage total.
         */
        fun bind(item: PlayerDamageItem) {
            Logger.d("DamageViewHolder: Binding data for player '${item.player.name}' with damage ${item.damage}.")

            // This handles placeholder items in the grid, making them invisible.
            if (item.player.playerIndex == -1) {
                Logger.d("DamageViewHolder: Player index is -1. Hiding view for position $adapterPosition.")
                itemView.visibility = View.INVISIBLE
                return
            }

            itemView.visibility = View.VISIBLE
            opponentName.text = item.player.name

            // Set the rotation of the text elements to match the overall segment's rotation.
            opponentName.rotation = angle.toFloat()
            damageAmount.rotation = angle.toFloat()

            // Set the background color of the damage counter based on the player's profile color.
            (damageAmount.background as? GradientDrawable)?.let { background ->
                item.player.color?.let {
                    val color = it.toColorInt()
                    // Blend with black to create a slightly darker shade for the background.
                    val darkerFillColor = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
                    background.setColor(darkerFillColor)
                    Logger.d("DamageViewHolder: Set background color for '${item.player.name}' to a shade of $it.")
                } ?: run {
                    background.setColor(defaultFillColor)
                    Logger.d("DamageViewHolder: Player '${item.player.name}' has no color. Using default background.")
                }
            }

            // Special handling for the item representing the player themselves.
            if (item.player.playerIndex == targetPlayerIndex) {
                Logger.d("DamageViewHolder: This item represents the target player. Disabling interactions.")
                damageAmount.text = itemView.context.getString(R.string.me)
                itemView.alpha = 0.6f // Make it look disabled.
                damageAmount.isClickable = false
                damageAmount.setOnLongClickListener(null)
                decrementButton.visibility = View.GONE
            } else {
                // Standard setup for opponent items.
                damageAmount.text = item.damage.toString()
                itemView.alpha = 1.0f
                damageAmount.isClickable = true

                // A single click increments the damage.
                damageAmount.setOnClickListener {
                    Logger.d("DamageViewHolder: Increment damage click for player '${item.player.name}'.")
                    decrementButton.visibility = View.GONE // Hide button on increment.
                    onDamageIncremented(item.player.playerIndex)
                }

                // A long click toggles the visibility of the decrement button.
                damageAmount.setOnLongClickListener {
                    Logger.d("DamageViewHolder: Long-click for player '${item.player.name}'. Toggling decrement button.")
                    decrementButton.visibility = if (decrementButton.isVisible) View.GONE else View.VISIBLE
                    true // Return true to consume the event.
                }

                // The decrement button itself simply decrements the damage.
                decrementButton.setOnClickListener {
                    Logger.d("DamageViewHolder: Decrement button click for player '${item.player.name}'.")
                    onDamageDecremented(item.player.playerIndex)
                }
            }
        }
    }
}

/**
 * A [DiffUtil.ItemCallback] for calculating the difference between two [PlayerDamageItem] lists.
 * This allows the [ListAdapter] to perform efficient, animated updates.
 */
class PlayerDamageDiffCallback : DiffUtil.ItemCallback<PlayerDamageItem>() {
    /**
     * Checks if two items represent the same entity.
     */
    override fun areItemsTheSame(oldItem: PlayerDamageItem, newItem: PlayerDamageItem): Boolean {
        // Players are unique by their index.
        return oldItem.player.playerIndex == newItem.player.playerIndex
    }

    /**
     * Checks if the contents of two items are the same.
     * This is called only if `areItemsTheSame` returns true.
     */
    override fun areContentsTheSame(oldItem: PlayerDamageItem, newItem: PlayerDamageItem): Boolean {
        // The data class's `equals` method compares all properties.
        return oldItem == newItem
    }
}