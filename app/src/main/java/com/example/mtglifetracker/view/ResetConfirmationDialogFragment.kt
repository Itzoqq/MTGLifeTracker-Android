package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class ResetConfirmationDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_reset_confirmation, null)

        val radioCurrent: RadioButton = view.findViewById(R.id.rb_reset_current)
        val playerCount = gameViewModel.gameState.value.playerCount
        radioCurrent.text = getString(R.string.reset_current_game_formatted, playerCount)

        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, null)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = "Confirm Reset"
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Reset") { _, _ ->
                val radioAll: RadioButton = view.findViewById(R.id.rb_reset_all)
                if (radioAll.isChecked) {
                    gameViewModel.resetAllGames()
                } else {
                    gameViewModel.resetCurrentGame()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }

    companion object {
        const val TAG = "ResetConfirmationDialog"
    }
}