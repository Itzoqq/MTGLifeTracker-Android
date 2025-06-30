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

class PlayerCountersDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Get the player's name from the arguments
        val nickname = arguments?.getString(ARG_NICKNAME) ?: "Player"

        // Inflate the empty view for the dialog's content area
        val view = inflater.inflate(R.layout.dialog_player_counters, null)

        // Inflate and set up the custom title
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        val backArrow = customTitleView.findViewById<ImageView>(R.id.iv_back_arrow)

        // Set the title and hide the back arrow
        titleTextView.text = getString(R.string.player_counters_title, nickname)
        backArrow.visibility = View.GONE

        // The positive button has been removed.
        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    companion object {
        const val TAG = "PlayerCountersDialogFragment"
        private const val ARG_NICKNAME = "nickname"

        // The newInstance method now only needs the player's name for the title.
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