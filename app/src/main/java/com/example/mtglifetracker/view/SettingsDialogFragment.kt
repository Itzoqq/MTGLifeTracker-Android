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
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main settings menu, presented as a [DialogFragment].
 *
 * This dialog displays a list of top-level settings options (e.g., "Number of Players",
 * "Manage Profiles"). It uses a [SettingsAdapter] to render the list. When a user
 * clicks on an item, this fragment is responsible for launching the appropriate
 * subsequent dialog fragment to handle that specific setting.
 */
@AndroidEntryPoint
class SettingsDialogFragment : DialogFragment() {

    /**
     * Called when the dialog is canceled. This ensures that if this master settings dialog
     * is closed, any other open dialogs are also dismissed.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("SettingsDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the dialog instance.
     * This method inflates the layout containing the RecyclerView and sets up the
     * [SettingsAdapter] with the list of options and the logic to handle clicks.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("SettingsDialog: onCreateDialog called.")
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Inflate the custom view that contains our RecyclerView.
        val view = inflater.inflate(R.layout.dialog_settings_menu, FrameLayout(requireContext()), false)
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_settings_options)

        // Define the list of options to be displayed in the settings menu.
        val settingsOptions = arrayOf("Number of Players", "Starting Life", "Manage Profiles", "Reset Game", "Preferences")

        // The adapter's click listener launches the appropriate sub-dialog based on the item position.
        val adapter = SettingsAdapter(settingsOptions) { position ->
            Logger.i("SettingsDialog: Item clicked at position $position ('${settingsOptions[position]}').")
            when (position) {
                0 -> {
                    Logger.d("SettingsDialog: Launching PlayerCountDialogFragment.")
                    PlayerCountDialogFragment().show(parentFragmentManager, PlayerCountDialogFragment.TAG)
                }
                1 -> {
                    Logger.d("SettingsDialog: Launching StartingLifeDialogFragment.")
                    StartingLifeDialogFragment().show(parentFragmentManager, StartingLifeDialogFragment.TAG)
                }
                2 -> {
                    Logger.d("SettingsDialog: Launching ManageProfilesDialogFragment.")
                    ManageProfilesDialogFragment().show(parentFragmentManager, ManageProfilesDialogFragment.TAG)
                }
                3 -> {
                    Logger.d("SettingsDialog: Launching ResetConfirmationDialogFragment.")
                    ResetConfirmationDialogFragment().show(parentFragmentManager, ResetConfirmationDialogFragment.TAG)
                }
                4 -> {
                    Logger.d("SettingsDialog: Launching PreferencesDialogFragment.")
                    PreferencesDialogFragment().show(parentFragmentManager, PreferencesDialogFragment.TAG)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Use a standard dialog title and set our custom view as the content.
        return builder
            .setTitle("Settings")
            .setView(view)
            .create()
    }

    /**
     * A companion object to hold constants related to the fragment.
     */
    companion object {
        // A unique tag for identifying this fragment in the FragmentManager.
        const val TAG = "SettingsDialogFragment"
    }
}