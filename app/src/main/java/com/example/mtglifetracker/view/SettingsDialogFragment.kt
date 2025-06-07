package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class SettingsDialogFragment : DialogFragment() {

    // Get a reference to the shared ViewModel.
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Add the new options to the settings menu.
        val settingsOptions = arrayOf("Number of Players", "Reset Current Game", "Reset All Games")

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, settingsOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Settings")
            .setAdapter(adapter) { dialog, which ->
                when (which) {
                    0 -> {
                        // Show the player count selection dialog.
                        parentFragmentManager.let {
                            PlayerCountDialogFragment().show(it, PlayerCountDialogFragment.TAG)
                        }
                    }
                    1 -> {
                        // Call the ViewModel to reset the current game.
                        gameViewModel.resetCurrentGame()
                    }
                    2 -> {
                        // Call the ViewModel to reset all games.
                        gameViewModel.resetAllGames()
                    }
                }
                // Dismiss this (the main settings) dialog.
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "SettingsDialogFragment"
    }
}