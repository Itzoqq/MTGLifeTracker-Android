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
import com.example.mtglifetracker.R

class SettingsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The list of options is now simplified.
        val settingsOptions = arrayOf("Number of Players", "Reset Game")

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
                        // Show the new reset confirmation dialog.
                        parentFragmentManager.let {
                            ResetConfirmationDialogFragment().show(it, ResetConfirmationDialogFragment.TAG)
                        }
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
