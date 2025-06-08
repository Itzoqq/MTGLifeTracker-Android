package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

/**
 * A DialogFragment that presents the user with a choice to reset either the current
 * game's data or all saved game data. This provides a crucial confirmation step
 * before performing a destructive action.
 */
class ResetConfirmationDialogFragment : DialogFragment() {

    // Get a reference to the shared ViewModel to access game state and trigger reset actions.
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the custom alert dialog style for a consistent theme.
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_reset_confirmation, null)

        val radioCurrent: RadioButton = view.findViewById(R.id.rb_reset_current)

        // Dynamically set the text for the "reset current" option to be more informative.
        // For example: "Reset current (4 players) game".
        val playerCount = gameViewModel.gameState.value.playerCount
        radioCurrent.text = getString(R.string.reset_current_game_formatted, playerCount)


        builder.setView(view)
            .setTitle("Confirm Reset")
            // The "positive" button executes the reset.
            .setPositiveButton("Reset") { _, _ ->
                val radioAll: RadioButton = view.findViewById(R.id.rb_reset_all)
                if (radioAll.isChecked) {
                    gameViewModel.resetAllGames()
                } else {
                    // Default action is to reset the current game.
                    gameViewModel.resetCurrentGame()
                }
            }
            // The "negative" button simply cancels the dialog.
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }

    companion object {
        const val TAG = "ResetConfirmationDialog"
    }
}
