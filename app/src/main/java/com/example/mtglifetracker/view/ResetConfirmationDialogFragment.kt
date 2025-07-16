package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.GameViewModel

/**
 * A [DialogFragment] that asks the user to confirm a game reset action.
 *
 * This dialog provides two options via radio buttons:
 * 1.  Reset only the currently active game layout (e.g., just the 4-player game).
 * 2.  Reset all game data across all player counts (a full reset).
 *
 * It communicates the user's choice to the [GameViewModel] to perform the appropriate
 * reset operation.
 */
class ResetConfirmationDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("ResetConfirmationDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the confirmation dialog instance.
     * This method inflates the layout, sets up the radio button text dynamically,
     * and configures the click listeners for the reset and cancel actions.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("ResetConfirmationDialog: onCreateDialog called.")
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Inflate the dialog's content view.
        val view = inflater.inflate(R.layout.dialog_reset_confirmation, FrameLayout(requireContext()), false)

        // Dynamically set the text for the "reset current" option to include the player count.
        val radioCurrent: RadioButton = view.findViewById(R.id.rb_reset_current)
        val playerCount = gameViewModel.gameState.value.playerCount
        radioCurrent.text = getString(R.string.reset_current_game_formatted, playerCount)
        Logger.d("ResetConfirmationDialog: Set 'reset current' radio button text for $playerCount players.")

        // Inflate and configure the custom title view.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_confirm_reset)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("ResetConfirmationDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Reset") { _, _ ->
                // This block executes when the user confirms the reset.
                val radioAll: RadioButton = view.findViewById(R.id.rb_reset_all)
                if (radioAll.isChecked) {
                    Logger.i("ResetConfirmationDialog: 'Reset All' option selected. Calling resetAllGames.")
                    gameViewModel.resetAllGames()
                } else {
                    Logger.i("ResetConfirmationDialog: 'Reset Current' option selected. Calling resetCurrentGame.")
                    gameViewModel.resetCurrentGame()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Logger.d("ResetConfirmationDialog: Cancel button clicked.")
                dialog.cancel()
            }

        return builder.create()
    }

    /**
     * A companion object to hold constants related to the fragment.
     */
    companion object {
        // A unique tag for identifying this fragment in the FragmentManager.
        const val TAG = "ResetConfirmationDialog"
    }
}