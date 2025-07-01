package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player

class CommanderDamageAdapter(
    private val allPlayers: List<Player>,
    private val targetPlayerIndex: Int,
    private var commanderDamages: Map<Int, Int>,
    private val onDamageClicked: (opponentIndex: Int) -> Unit
) : RecyclerView.Adapter<CommanderDamageAdapter.DamageViewHolder>() {

    fun updateDamage(newDamages: Map<Int, Int>) {
        this.commanderDamages = newDamages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DamageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_commander_damage, parent, false)
        return DamageViewHolder(view, targetPlayerIndex)
    }

    override fun onBindViewHolder(holder: DamageViewHolder, position: Int) {
        val player = allPlayers[position]
        // For opponents, get their damage value. For "Me", it's irrelevant, so default to 0.
        val damage = commanderDamages[player.playerIndex] ?: 0
        holder.bind(player, damage, onDamageClicked)
    }

    override fun getItemCount(): Int = allPlayers.size

    class DamageViewHolder(itemView: View, private val targetPlayerIndex: Int) : RecyclerView.ViewHolder(itemView) {
        private val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        private val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        private val defaultFillColor = ContextCompat.getColor(itemView.context, R.color.default_segment_background)

        fun bind(player: Player, damage: Int, onDamageClicked: (opponentIndex: Int) -> Unit) {
            opponentName.text = player.name

            val background = damageAmount.background as GradientDrawable

            // Set the background color first
            player.color?.let {
                val color = it.toColorInt()
                val darkerFillColor = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
                background.setColor(darkerFillColor)
            } ?: run {
                background.setColor(defaultFillColor)
            }

            // *** THE FIX IS HERE ***
            // Check if the current player is the one who opened the dialog
            if (player.playerIndex == targetPlayerIndex) {
                damageAmount.text = itemView.context.getString(R.string.me) // Use a string resource for "Me"
                damageAmount.isClickable = false // You can't deal damage to yourself
                // Optionally make it look different, e.g., slightly transparent
                itemView.alpha = 0.6f
            } else {
                damageAmount.text = damage.toString()
                damageAmount.isClickable = true
                itemView.alpha = 1.0f
                damageAmount.setOnClickListener {
                    onDamageClicked(player.playerIndex)
                }
            }
        }
    }
}