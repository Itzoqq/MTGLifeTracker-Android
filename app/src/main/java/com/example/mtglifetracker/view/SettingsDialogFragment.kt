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
        val settingsOptions = arrayOf("Number of Players", "Starting Life", "Manage profiles", "Reset Game")

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
                        PlayerCountDialogFragment().show(parentFragmentManager, PlayerCountDialogFragment.TAG)
                    }
                    1 -> {
                        StartingLifeDialogFragment().show(parentFragmentManager, StartingLifeDialogFragment.TAG)
                    }
                    2 -> {
                        // "Create a profile" - No action yet
                    }
                    3 -> {
                        ResetConfirmationDialogFragment().show(parentFragmentManager, ResetConfirmationDialogFragment.TAG)
                    }
                }
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "SettingsDialogFragment"
    }
}