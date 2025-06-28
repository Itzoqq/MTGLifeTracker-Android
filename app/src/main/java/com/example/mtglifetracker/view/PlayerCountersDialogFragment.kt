package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
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
        val view = inflater.inflate(R.layout.dialog_player_counters, FrameLayout(requireContext()), false)

        val nickname = arguments?.getString(ARG_NICKNAME) ?: "Player"
        val rotation = arguments?.getFloat(ARG_ROTATION) ?: 0f

        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        val closeButton = customTitleView.findViewById<ImageView>(R.id.iv_back_arrow)

        titleTextView.text = getString(R.string.player_counters_title, nickname)
        // Use the 'X' icon and have it dismiss the dialog
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        closeButton.setOnClickListener { dismiss() }

        // Remove the positive button
        builder.setCustomTitle(customTitleView)
            .setView(view)

        val dialog = builder.create()

        // Apply rotation to the dialog's window
        dialog.setOnShowListener {
            dialog.window?.decorView?.rotation = rotation
        }

        return dialog
    }

    companion object {
        const val TAG = "PlayerCountersDialogFragment"
        private const val ARG_NICKNAME = "nickname"
        private const val ARG_ROTATION = "rotation"

        fun newInstance(nickname: String, rotation: Float): PlayerCountersDialogFragment {
            val args = Bundle().apply {
                putString(ARG_NICKNAME, nickname)
                putFloat(ARG_ROTATION, rotation)
            }
            return PlayerCountersDialogFragment().apply {
                arguments = args
            }
        }
    }
}