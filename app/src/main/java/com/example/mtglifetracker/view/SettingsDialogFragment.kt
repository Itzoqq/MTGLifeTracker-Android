package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsDialogFragment : DialogFragment() {

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Inflate the custom view that contains our RecyclerView
        val view = inflater.inflate(R.layout.dialog_settings_menu, FrameLayout(requireContext()), false)
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_settings_options)

        val settingsOptions = arrayOf("Number of Players", "Starting Life", "Manage Profiles", "Reset Game")

        // The adapter's click listener launches the sub-dialogs WITHOUT dismissing this one.
        val adapter = SettingsAdapter(settingsOptions) { position ->
            when (position) {
                0 -> PlayerCountDialogFragment().show(parentFragmentManager, PlayerCountDialogFragment.TAG)
                1 -> StartingLifeDialogFragment().show(parentFragmentManager, StartingLifeDialogFragment.TAG)
                2 -> ManageProfilesDialogFragment().show(parentFragmentManager, ManageProfilesDialogFragment.TAG)
                3 -> ResetConfirmationDialogFragment().show(parentFragmentManager, ResetConfirmationDialogFragment.TAG)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Use a standard title and set our custom view as the content
        return builder
            .setTitle("Settings")
            .setView(view)
            .create()
    }

    companion object {
        const val TAG = "SettingsDialogFragment"
    }
}