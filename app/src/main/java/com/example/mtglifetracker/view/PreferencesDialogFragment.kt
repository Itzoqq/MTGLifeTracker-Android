package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.PreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * A [DialogFragment] for displaying and modifying user-specific application preferences.
 *
 * This dialog currently contains a single switch to control whether commander damage
 * should be automatically deducted from a player's life total. It observes the
 * [PreferencesViewModel] to display the current setting and calls the ViewModel to
 * save any changes.
 */
@AndroidEntryPoint
class PreferencesDialogFragment : DialogFragment() {

    private val preferencesViewModel: PreferencesViewModel by viewModels()

    /**
     * Creates and configures the dialog instance.
     * This method inflates the layout, sets up the custom title, and wires up the
     * preference switch to observe and update the [PreferencesViewModel].
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("PreferencesDialog: onCreateDialog called.")
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_preferences, FrameLayout(requireContext()), false)
        val switch: SwitchCompat = view.findViewById(R.id.switch_auto_deduce_damage)

        // Launch a lifecycle-aware coroutine to observe the preferences state.
        lifecycleScope.launch {
            preferencesViewModel.preferences.collect { preferences ->
                // This block will run every time the preferences state changes.
                Logger.d("PreferencesDialog: Preferences flow collected new state. deduceCommanderDamage=${preferences.deduceCommanderDamage}")
                // Update the switch's checked state to reflect the latest preference value.
                switch.isChecked = preferences.deduceCommanderDamage
            }
        }

        // Set a listener to react to the user toggling the switch.
        switch.setOnCheckedChangeListener { _, isChecked ->
            Logger.i("PreferencesDialog: 'Deduce Damage' switch toggled. New value: $isChecked.")
            // Call the ViewModel to save the new preference state.
            preferencesViewModel.setDeduceCommanderDamage(isChecked)
        }

        // Inflate and configure the custom title view.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_preferences)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("PreferencesDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    /**
     * A companion object to hold constants related to the fragment.
     */
    companion object {
        // A unique tag for identifying this fragment in the FragmentManager.
        const val TAG = "PreferencesDialogFragment"
    }
}