package com.example.mtglifetracker.view

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import androidx.core.graphics.drawable.toDrawable

class CommanderDamageDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    private lateinit var adapter: CommanderDamageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the correct layout based on the player's rotation angle
        val angle = requireArguments().getInt(ARG_ANGLE)
        val layoutId = when (angle) {
            90 -> R.layout.dialog_commander_damage_rotated
            -90 -> R.layout.dialog_commander_damage_rotated_left
            180 -> R.layout.dialog_commander_damage_rotated_180
            else -> R.layout.dialog_commander_damage
        }
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetPlayerIndex = requireArguments().getInt(ARG_TARGET_PLAYER_INDEX)
        val targetPlayer = gameViewModel.gameState.value.players.find { it.playerIndex == targetPlayerIndex }!!
        val angle = requireArguments().getInt(ARG_ANGLE)

        // Handle the title view based on which layout was inflated
        if (angle == 90 || angle == -90) {
            val verticalTitleView: VerticalTextView = view.findViewById(R.id.tv_dialog_content_title)
            verticalTitleView.text = getString(R.string.player_commander_damage_title, targetPlayer.name)
            // Set the drawing direction based on the angle
            verticalTitleView.isTopDown = (angle == 90)
        } else {
            val titleTextView: TextView = view.findViewById(R.id.tv_dialog_content_title)
            titleTextView.text = getString(R.string.player_commander_damage_title, targetPlayer.name)
            // Also apply 180 degree rotation to the title
            titleTextView.rotation = angle.toFloat()
        }

        setupRecyclerView(view, targetPlayerIndex)
    }

    override fun onStart() {
        super.onStart()
        // Set dialog window properties for a custom layout
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            // Make the dialog a square based on 90% of screen width
            val size = (displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(size, size)
            window.setGravity(Gravity.CENTER)
            // Make the default window background transparent so our custom background shows
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    private fun setupRecyclerView(view: View, targetPlayerIndex: Int) {
        val allPlayers = gameViewModel.gameState.value.players
        val playerCount = allPlayers.size
        val angle = requireArguments().getInt(ARG_ANGLE)
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

        val spanCount = if (playerCount == 2) 1 else 2
        val layoutManager = GridLayoutManager(context, spanCount)

        if (playerCount == 3) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) 2 else 1
                }
            }
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                val damageMap = damages.associate { it.sourcePlayerIndex to it.damage }
                val initialItems = allPlayers.map { PlayerDamageItem(it, damageMap[it.playerIndex] ?: 0) }

                val finalList = if (playerCount == 5) {
                    mutableListOf<PlayerDamageItem>().apply {
                        val placeholder = PlayerDamageItem(Player(gameSize = 5, playerIndex = -1, life = 0), 0)
                        add(placeholder)
                        add(initialItems[2])
                        add(initialItems[0])
                        add(initialItems[3])
                        add(initialItems[1])
                        add(initialItems[4])
                    }
                } else {
                    initialItems
                }
                adapter.submitList(finalList)
            }
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