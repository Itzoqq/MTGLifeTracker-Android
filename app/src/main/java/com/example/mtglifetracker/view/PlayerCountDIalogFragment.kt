package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.GameViewModel

/**
 * A [DialogFragment] that allows the user to select the number of players for the game.
 *
 * This dialog presents a simple list of options (from 2 to 6 players). When an option is
 * selected, it calls the corresponding method in the [GameViewModel] to update the
 * application's state, which in turn causes the main UI to re-render with the correct
 * number of player segments.
 */
class PlayerCountDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("PlayerCountDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the dialog instance.
     * This method sets up the list of player count options, the adapter to display them,
     * and the click listener to handle the user's selection.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("PlayerCountDialog: onCreateDialog called.")
        val playerCountOptions = arrayOf("2", "3", "4", "5", "6")

        // Create a custom ArrayAdapter to ensure the text color is white, matching the dialog's theme.
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, playerCountOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        // Inflate the custom title view.
        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)

        // Set the title from string resources and configure the back arrow's click listener.
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_player_count)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("PlayerCountDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        // Build the AlertDialog, setting the custom title and adapter.
        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
            .setAdapter(adapter) { _, which ->
                // This block executes when the user selects an item from the list.
                val defaultPlayerCount = gameViewModel.gameState.value.playerCount
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: defaultPlayerCount
                Logger.i("PlayerCountDialog: User selected '$selectedPlayerCount' players.")

                // Call the ViewModel to update the game state with the new player count.
                gameViewModel.changePlayerCount(selectedPlayerCount)
            }
            .create()
    }

    /**
     * A companion object to hold constants related to the fragment.
     */
    companion object {
        // A unique tag for identifying this fragment in the FragmentManager.
        const val TAG = "PlayerCountDialogFragment"
    }
}