package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class CustomLifeDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_custom_life, FrameLayout(requireContext()), false)
        val editText = view.findViewById<EditText>(R.id.et_custom_life)

        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_custom_starting_life)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Set", null) // Set listener to null initially
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()

        // Override the button's behavior to prevent it from closing automatically
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val lifeString = editText.text.toString()
                if (lifeString.isNotEmpty()) {
                    val life = lifeString.toInt()
                    if (life in 1..999) {
                        gameViewModel.changeStartingLife(life)
                        dismiss() // Dismiss only if input is valid
                    } else {
                        Toast.makeText(requireContext(), "Life must be between 1 and 999", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return dialog
    }

    companion object {
        const val TAG = "CustomLifeDialogFragment"
    }
}