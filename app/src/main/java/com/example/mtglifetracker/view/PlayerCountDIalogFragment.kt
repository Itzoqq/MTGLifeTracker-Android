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

/**
 * A dedicated DialogFragment for handling player count selection.
 *
 * It is launched by the SettingsDialogFragment and is responsible for
 * displaying the player count options and updating the shared GameViewModel.
 */
class PlayerCountDialogFragment : DialogFragment() {

    // Get the same shared ViewModel instance from the hosting Activity.
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playerCountOptions = arrayOf("2", "3", "4", "5", "6")

        // Custom adapter to make list item text white.
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, playerCountOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Number of Players")
            .setAdapter(adapter) { dialog, which ->
                val defaultPlayerCount = gameViewModel.gameState.value.playerCount
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: defaultPlayerCount

                // Update the state in the shared ViewModel.
                gameViewModel.changePlayerCount(selectedPlayerCount)

                // The dialog will be dismissed automatically on selection.
            }
            .create()
    }

    companion object {
        // Tag for the FragmentManager to identify this dialog.
        const val TAG = "PlayerCountDialogFragment"
    }
}