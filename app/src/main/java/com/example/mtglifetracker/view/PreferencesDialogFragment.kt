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
import com.example.mtglifetracker.viewmodel.PreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PreferencesDialogFragment : DialogFragment() {

    private val preferencesViewModel: PreferencesViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_preferences, FrameLayout(requireContext()), false)
        val switch: SwitchCompat = view.findViewById(R.id.switch_auto_deduce_damage)

        lifecycleScope.launch {
            preferencesViewModel.preferences.collect { preferences ->
                switch.isChecked = preferences.deduceCommanderDamage
            }
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            preferencesViewModel.setDeduceCommanderDamage(isChecked)
        }

        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_preferences)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    companion object {
        const val TAG = "PreferencesDialogFragment"
    }
}