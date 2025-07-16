package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger

/**
 * A [DialogFragment] for displaying various player-specific counters.
 *
 * This dialog currently serves as a placeholder for future functionality. Its primary
 * role is to provide a dedicated space where counters like poison, energy, or storm count
 * could be managed for a specific player. It features a custom title that includes the
 * player's name.
 */
class PlayerCountersDialogFragment : DialogFragment() {

    /**
     * Creates and configures the dialog instance.
     * This method inflates the dialog's layout and sets up the custom title with
     * the player's name retrieved from the fragment's arguments.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Get the player's name from the arguments to customize the title.
        val nickname = arguments?.getString(ARG_NICKNAME) ?: "Player"
        Logger.i("PlayerCountersDialog: onCreateDialog for player '$nickname'.")

        // Inflate the empty view for the dialog's content area.
        // This is a placeholder for where counter UI would be added.
        val view = inflater.inflate(R.layout.dialog_player_counters, null)

        // Inflate and set up the custom title view.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        val backArrow = customTitleView.findViewById<ImageView>(R.id.iv_back_arrow)

        // Set the title using a formatted string resource.
        titleTextView.text = getString(R.string.player_counters_title, nickname)
        // The back arrow is not needed in this simple version of the dialog.
        backArrow.visibility = View.GONE
        Logger.d("PlayerCountersDialog: Custom title set to '${titleTextView.text}'.")

        // No positive/negative buttons are needed at this time.
        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    /**
     * A companion object to provide a standardized factory method for creating instances
     * of this fragment.
     */
    companion object {
        const val TAG = "PlayerCountersDialogFragment"
        private const val ARG_NICKNAME = "nickname"

        /**
         * Creates a new instance of [PlayerCountersDialogFragment].
         *
         * @param nickname The name of the player, used to customize the dialog title.
         * @return A new fragment instance with the required nickname argument.
         */
        fun newInstance(nickname: String): PlayerCountersDialogFragment {
            val args = Bundle().apply {
                putString(ARG_NICKNAME, nickname)
            }
            return PlayerCountersDialogFragment().apply {
                arguments = args
            }
        }
    }
}