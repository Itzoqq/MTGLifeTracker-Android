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

/**
 * A DialogFragment that encapsulates the main settings menu.
 *
 * This fragment's responsibility is now only to show the top-level settings.
 * It navigates to other DialogFragments for specific settings.
 */
class SettingsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val settingsOptions = arrayOf("Number of Players")

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
                if (which == 0) {
                    // *** CORRECTED LOGIC ***
                    // Instead of showing an AlertDialog, we show our new PlayerCountDialogFragment.
                    // We use parentFragmentManager to ensure it's managed correctly.
                    parentFragmentManager.let {
                        PlayerCountDialogFragment().show(it, PlayerCountDialogFragment.TAG)
                    }
                }
                // Dismiss this (the main settings) dialog.
                dialog.dismiss()
            }
            .create()
    }

    // *** REMOVED ***
    // The showPlayerCountSelection() method has been moved to its own Fragment.

    companion object {
        const val TAG = "SettingsDialogFragment"
    }
}