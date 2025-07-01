package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CommanderDamageDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    private lateinit var adapter: CommanderDamageAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        val targetPlayerIndex = requireArguments().getInt(ARG_TARGET_PLAYER_INDEX)
        val targetPlayer = gameViewModel.gameState.value.players[targetPlayerIndex]

        // *** THE FIX IS HERE ***
        // Get all players, not just opponents
        val allPlayers = gameViewModel.gameState.value.players

        val view = inflater.inflate(R.layout.dialog_commander_damage, FrameLayout(requireContext()), false)
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_commander_damage)

        // Pass all players and the target index to the adapter
        adapter = CommanderDamageAdapter(allPlayers, targetPlayerIndex, emptyMap()) { opponentIndex ->
            gameViewModel.incrementCommanderDamage(opponentIndex, targetPlayerIndex)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        lifecycleScope.launch {
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                val damageMap = damages.associate { it.sourcePlayerIndex to it.damage }
                adapter.updateDamage(damageMap)
            }
        }

        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)

        titleTextView.text = getString(R.string.commander_damage_to_player, targetPlayer.name)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
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