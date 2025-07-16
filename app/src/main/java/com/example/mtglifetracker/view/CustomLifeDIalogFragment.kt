package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * A [DialogFragment] that allows the user to enter a custom starting life total.
 *
 * This dialog presents an [EditText] for numeric input. It includes validation to ensure
 * the entered value is within a reasonable range (1-999) before applying the change.
 * It features a custom title and overrides the positive button's behavior to prevent
 * the dialog from closing on invalid input.
 */
class CustomLifeDialogFragment : DialogFragment() {

    // Get a reference to the shared GameViewModel using the Hilt activityViewModels delegate.
    private val gameViewModel: GameViewModel by activityViewModels()

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("CustomLifeDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the dialog instance.
     * This method inflates the custom layout, sets up the title and input validation logic,
     * and constructs the AlertDialog.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("CustomLifeDialog: onCreateDialog called.")
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_custom_life, FrameLayout(requireContext()), false)
        val editText = view.findViewById<EditText>(R.id.et_custom_life)

        // Inflate and configure the custom title view.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_custom_starting_life)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("CustomLifeDialog: Back arrow in title clicked. Dismissing.")
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            // Set the positive button's listener to null initially. We will override it in setOnShowListener
            // to prevent the dialog from closing automatically when the button is pressed.
            .setPositiveButton("Set", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                Logger.d("CustomLifeDialog: Cancel button clicked.")
                dialog.cancel()
            }

        val dialog = builder.create()

        // Override the positive button's behavior after the dialog is shown.
        dialog.setOnShowListener {
            Logger.d("CustomLifeDialog: onShowListener triggered. Overriding positive button behavior.")
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val lifeString = editText.text.toString()
                Logger.d("CustomLifeDialog: 'Set' button clicked. Input value: '$lifeString'.")

                if (lifeString.isNotEmpty()) {
                    val life = lifeString.toInt()
                    // Validate that the entered life is within the allowed range.
                    if (life in 1..999) {
                        Logger.i("CustomLifeDialog: Input is valid. Setting starting life to $life.")
                        gameViewModel.changeStartingLife(life)
                        dismiss() // Dismiss the dialog only if the input is valid.
                    } else {
                        Logger.w("CustomLifeDialog: Validation failed. Life total $life is not between 1 and 999.")
                        Snackbar.make(view, "Life must be between 1 and 999", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Logger.w("CustomLifeDialog: Validation failed. Input string is empty.")
                    // Optionally, you could show a Snackbar here as well.
                }
            }
        }

        return dialog
    }

    /**
     * A companion object to hold constants related to the fragment.
     */
    companion object {
        // A unique tag for identifying this fragment in the FragmentManager.
        const val TAG = "CustomLifeDialogFragment"
    }
}