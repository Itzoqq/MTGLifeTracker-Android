package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
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
import androidx.core.view.isVisible

class CommanderDamageDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    private lateinit var adapter: CommanderDamageAdapter
    private val manualPlayerViews = mutableMapOf<Int, View>()

    override fun onResume() {
        super.onResume()
        // Set the dialog to a fixed square size
        val window = dialog?.window ?: return
        val displayMetrics = resources.displayMetrics
        // Set size to 90% of the screen width
        val size = (displayMetrics.widthPixels * 0.90).toInt()
        window.setLayout(size, size)
        window.setGravity(Gravity.CENTER)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        val targetPlayerIndex = requireArguments().getInt(ARG_TARGET_PLAYER_INDEX)
        val targetPlayer = gameViewModel.gameState.value.players.find { it.playerIndex == targetPlayerIndex }!!
        val allPlayers = gameViewModel.gameState.value.players
        val playerCount = allPlayers.size

        val view = inflater.inflate(R.layout.dialog_commander_damage, FrameLayout(requireContext()), false)

        if (playerCount == 5) {
            setupFivePlayerLayout(view, allPlayers, targetPlayerIndex, inflater)
        } else {
            setupDefaultLayout(view, allPlayers, targetPlayerIndex)
        }

        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)

        titleTextView.text = getString(R.string.player_commander_damage_title, targetPlayer.name)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).visibility = View.GONE

        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    private fun setupDefaultLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int) {
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_commander_damage)
        recyclerView.visibility = View.VISIBLE

        adapter = CommanderDamageAdapter(
            targetPlayerIndex,
            onDamageIncremented = { opponentIndex ->
                gameViewModel.incrementCommanderDamage(opponentIndex, targetPlayerIndex)
            },
            onDamageDecremented = { opponentIndex ->
                gameViewModel.decrementCommanderDamage(opponentIndex, targetPlayerIndex)
            }
        )

        val playerCount = allPlayers.size
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
                adapter.submitList(initialItems)
            }
        }
    }

    private fun setupFivePlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, inflater: LayoutInflater) {
        val rootContainer = view as FrameLayout
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_commander_damage)
        recyclerView.visibility = View.GONE

        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            weightSum = 2f
        }

        val col1 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }

        val col2 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }

        allPlayers.forEach { player ->
            val playerView = inflater.inflate(R.layout.item_commander_damage, rootContainer, false)
            manualPlayerViews[player.playerIndex] = playerView

            if (player.playerIndex == 0 || player.playerIndex == 1) {
                col1.addView(playerView)
            } else {
                col2.addView(playerView)
            }
        }

        horizontalLayout.addView(col1)
        horizontalLayout.addView(col2)
        rootContainer.addView(horizontalLayout)

        lifecycleScope.launch {
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                val damageMap = damages.associate { it.sourcePlayerIndex to it.damage }
                allPlayers.forEach { player ->
                    val playerView = manualPlayerViews[player.playerIndex] ?: return@forEach
                    val damage = damageMap[player.playerIndex] ?: 0
                    bindManualPlayerView(playerView, player, damage, targetPlayerIndex)
                }
            }
        }
    }

    private fun bindManualPlayerView(view: View, player: Player, damage: Int, targetPlayerIndex: Int) {
        val opponentName: TextView = view.findViewById(R.id.tv_opponent_name)
        val damageAmount: TextView = view.findViewById(R.id.tv_commander_damage)
        val decrementButton: ImageView = view.findViewById(R.id.iv_decrement_button)
        val defaultFillColor = ContextCompat.getColor(view.context, R.color.default_segment_background)

        opponentName.text = player.name
        val background = damageAmount.background as GradientDrawable

        player.color?.let {
            val color = it.toColorInt()
            val darkerFillColor = ColorUtils.blendARGB(color, android.graphics.Color.BLACK, 0.2f)
            background.setColor(darkerFillColor)
        } ?: run {
            background.setColor(defaultFillColor)
        }

        if (player.playerIndex == targetPlayerIndex) {
            damageAmount.text = view.context.getString(R.string.me)
            view.alpha = 0.6f
            damageAmount.isClickable = false
            decrementButton.visibility = View.GONE
        } else {
            damageAmount.text = damage.toString()
            view.alpha = 1.0f
            damageAmount.isClickable = true
            damageAmount.setOnClickListener {
                decrementButton.visibility = View.GONE
                gameViewModel.incrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }
            damageAmount.setOnLongClickListener {
                decrementButton.visibility = if (decrementButton.isVisible) View.GONE else View.VISIBLE
                true
            }
            decrementButton.setOnClickListener {
                gameViewModel.decrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }
        }
    }

    companion object {
        const val TAG = "CommanderDamageDialogFragment"
        private const val ARG_TARGET_PLAYER_INDEX = "target_player_index"

        fun newInstance(targetPlayerIndex: Int): CommanderDamageDialogFragment {
            return CommanderDamageDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TARGET_PLAYER_INDEX, targetPlayerIndex)
                }
            }
        }
    }
}