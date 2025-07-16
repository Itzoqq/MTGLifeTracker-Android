package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * A [DialogFragment] that asks the user for final confirmation before deleting a profile.
 *
 * This dialog is crucial for preventing accidental data loss. It displays a clear
 * message indicating which profile is about to be deleted and provides a "DELETE" button
 * styled in red to emphasize the destructive nature of the action.
 */
@AndroidEntryPoint
class DeleteConfirmationDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("DeleteConfirmationDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the confirmation dialog instance.
     * It retrieves the profile ID and name from the arguments to display a specific
     * confirmation message and to perform the correct deletion action.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Retrieve the profile details from the fragment's arguments.
        val profileId = arguments?.getLong(ARG_PROFILE_ID) ?: -1
        val profileName = arguments?.getString(ARG_PROFILE_NAME) ?: "this profile"
        Logger.i("DeleteConfirmationDialog: onCreateDialog for Profile ID: $profileId, Name: '$profileName'.")

        // Inflate the custom title view.
        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_delete_profile)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("DeleteConfirmationDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        // Build the AlertDialog.
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
            .setMessage("Are you sure you want to delete '$profileName'?")
            .setNegativeButton("Cancel") { dialog, _ ->
                // The user backed out of the deletion.
                Logger.d("DeleteConfirmationDialog: Cancel button clicked.")
                dialog.cancel()
            }
            .setPositiveButton("DELETE") { _, _ ->
                // The user confirmed the deletion.
                if (profileId != -1L) {
                    Logger.i("DeleteConfirmationDialog: DELETE button confirmed for profile ID: $profileId.")
                    profileViewModel.deleteProfile(profileId)
                } else {
                    // This case should ideally never happen if the dialog is created correctly.
                    Logger.e(null, "DeleteConfirmationDialog: DELETE button clicked with an invalid profile ID (-1).")
                }
            }

        val dialog = builder.create()

        // After the dialog is created but before it is shown, we can style the buttons.
        dialog.setOnShowListener {
            Logger.d("DeleteConfirmationDialog: onShowListener triggered. Styling dialog buttons.")
            // Set the positive button's text color to red to indicate a destructive action.
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.RED)
            // Ensure the negative button has standard text color.
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(Color.WHITE)
        }

        return dialog
    }

    /**
     * A companion object to provide a standardized factory method for creating instances
     * of this fragment, ensuring the required arguments are always passed correctly.
     */
    companion object {
        const val TAG = "DeleteConfirmationDialogFragment"
        private const val ARG_PROFILE_ID = "profile_id"
        private const val ARG_PROFILE_NAME = "profile_name"

        /**
         * Creates a new instance of [DeleteConfirmationDialogFragment].
         *
         * @param profileId The unique ID of the profile to be deleted.
         * @param profileName The name of the profile, to be displayed in the confirmation message.
         * @return A new fragment instance with the required arguments.
         */
        fun newInstance(profileId: Long, profileName: String): DeleteConfirmationDialogFragment {
            val args = Bundle().apply {
                putLong(ARG_PROFILE_ID, profileId)
                putString(ARG_PROFILE_NAME, profileName)
            }
            return DeleteConfirmationDialogFragment().apply {
                arguments = args
            }
        }
    }
}