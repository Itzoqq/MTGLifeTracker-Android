package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class ResetConfirmationDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // FIX: Provide a temporary parent to resolve layout parameters
        val view = inflater.inflate(R.layout.dialog_reset_confirmation, FrameLayout(requireContext()), false)

        val radioCurrent: RadioButton = view.findViewById(R.id.rb_reset_current)
        val playerCount = gameViewModel.gameState.value.playerCount
        radioCurrent.text = getString(R.string.reset_current_game_formatted, playerCount)

        // FIX: Provide a temporary parent to resolve layout parameters
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)

        // FIX: Use string resource instead of a hardcoded literal
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_confirm_reset)
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