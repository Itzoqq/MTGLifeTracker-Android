package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class CustomLifeDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_custom_life, null)
        val editText = view.findViewById<EditText>(R.id.et_custom_life)

        builder.setView(view)
            .setTitle("Custom Starting Life")
            .setPositiveButton("Set") { _, _ ->
                val lifeString = editText.text.toString()
                if (lifeString.isNotEmpty()) {
                    val life = lifeString.toInt()
                    if (life in 1..999) { // Updated the range to 999
                        gameViewModel.changeStartingLife(life)
                    } else {
                        // Updated the toast message to reflect the new range
                        Toast.makeText(requireContext(), "Life must be between 1 and 999", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }

    companion object {
        const val TAG = "CustomLifeDialogFragment"
    }
}