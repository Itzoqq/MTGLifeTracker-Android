package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CommanderDamageDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    private var adapter: CommanderDamageAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val playerCount = gameViewModel.gameState.value.playerCount
        val angle = requireArguments().getInt(ARG_ANGLE)

        val layoutId = if (playerCount == 5) {
            when (angle) {
                90 -> R.layout.dialog_commander_damage_5_player_rotated_right
                -90 -> R.layout.dialog_commander_damage_5_player_rotated_left
                else -> -1
            }
        } else {
            when (angle) {
                90 -> R.layout.dialog_commander_damage_rotated_right
                -90 -> R.layout.dialog_commander_damage_rotated_left
                180 -> R.layout.dialog_commander_damage_rotated_180
                else -> R.layout.dialog_commander_damage
            }
        }

        if (layoutId == -1) {
            dismiss()
            return null
        }
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetPlayerIndex = requireArguments().getInt(ARG_TARGET_PLAYER_INDEX)
        val allPlayers = gameViewModel.gameState.value.players
        val targetPlayer = allPlayers.find { it.playerIndex == targetPlayerIndex }!!
        val playerCount = allPlayers.size
        val angle = requireArguments().getInt(ARG_ANGLE)

        val titleView: TextView = view.findViewById(R.id.tv_dialog_content_title)
        titleView.text = getString(R.string.player_commander_damage_title, targetPlayer.name)
        if (titleView is VerticalTextView) {
            titleView.isTopDown = (angle == 90)
        }

        if (playerCount == 5) {
            setup5PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
        } else {
            setupRecyclerView(view, allPlayers, targetPlayerIndex, angle)
        }
    }

    private fun setup5PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val playersForLayout = allPlayers.sortedBy { it.playerIndex }

        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)

        val itemViews = listOfNotNull(
            leftColumn.getChildAt(0), leftColumn.getChildAt(1),
            rightColumn.getChildAt(0), rightColumn.getChildAt(1), rightColumn.getChildAt(2)
        )

        lifecycleScope.launch {
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                val damageMap = damages.associate { it.sourcePlayerIndex to it.damage }

                itemViews.forEachIndexed { index, itemView ->
                    val player = playersForLayout.getOrNull(index)
                    if (player != null) {
                        itemView.visibility = View.VISIBLE
                        val damage = damageMap[player.playerIndex] ?: 0
                        bindDamageView(itemView, player, damage, targetPlayerIndex, angle)
                    } else {
                        itemView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun bindDamageView(itemView: View, player: Player, damage: Int, targetPlayerIndex: Int, angle: Int) {
        val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        val decrementButton: ImageView = itemView.findViewById(R.id.iv_decrement_button)

        opponentName.rotation = angle.toFloat()
        damageAmount.rotation = angle.toFloat()

        // --- THIS IS THE FIX ---
        // Always show the player's actual name above the box.
        opponentName.text = player.name

        (damageAmount.background as? GradientDrawable)?.let { background ->
            val color = player.color?.toColorInt()
                ?: ContextCompat.getColor(requireContext(), R.color.default_segment_background)
            background.setColor(color)
        }

        // If this box is for the player who opened the dialog...
        if (player.playerIndex == targetPlayerIndex) {
            // ...put "Me" inside the box and disable it.
            damageAmount.text = getString(R.string.me)
            itemView.alpha = 0.6f
            damageAmount.setOnClickListener(null)
            damageAmount.setOnLongClickListener(null)
            decrementButton.isVisible = false
        } else {
            // Otherwise, show the damage and enable interactions.
            damageAmount.text = damage.toString()
            itemView.alpha = 1.0f

            damageAmount.setOnClickListener {
                gameViewModel.incrementCommanderDamage(player.playerIndex, targetPlayerIndex)
                decrementButton.isVisible = false
            }
            damageAmount.setOnLongClickListener {
                decrementButton.isVisible = !decrementButton.isVisible
                true
            }
            decrementButton.setOnClickListener {
                gameViewModel.decrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }
        }
    }

    private fun setupRecyclerView(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val playerCount = allPlayers.size
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_commander_damage)

        adapter = CommanderDamageAdapter(
            targetPlayerIndex,
            angle,
            onDamageIncremented = { opponentIndex ->
                gameViewModel.incrementCommanderDamage(opponentIndex, targetPlayerIndex)
            },
            onDamageDecremented = { opponentIndex ->
                gameViewModel.decrementCommanderDamage(opponentIndex, targetPlayerIndex)
            }
        )

        val spanCount = if (playerCount < 3) 1 else 2
        val layoutManager = GridLayoutManager(context, spanCount)

        if (playerCount == 3) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (position == 0) 2 else 1
            }
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                val damageMap = damages.associate { it.sourcePlayerIndex to it.damage }
                val initialItems = allPlayers.map { PlayerDamageItem(it, damageMap[it.playerIndex] ?: 0) }
                val finalList = if (playerCount == 3) {
                    val me = initialItems.find { it.player.playerIndex == targetPlayerIndex }!!
                    val opponents = initialItems.filter { it.player.playerIndex != targetPlayerIndex }
                    listOf(me, opponents[0], opponents[1])
                } else {
                    initialItems
                }
                adapter?.submitList(finalList)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val size = (displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(size, size)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    companion object {
        const val TAG = "CommanderDamageDialogFragment"
        private const val ARG_TARGET_PLAYER_INDEX = "target_player_index"
        private const val ARG_ANGLE = "angle"

        fun newInstance(targetPlayerIndex: Int, angle: Int): CommanderDamageDialogFragment {
            return CommanderDamageDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TARGET_PLAYER_INDEX, targetPlayerIndex)
                    putInt(ARG_ANGLE, angle)
                }
            }
        }
    }
}