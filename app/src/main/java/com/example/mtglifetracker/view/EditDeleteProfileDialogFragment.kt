package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.AndroidEntryPoint

/**
 * A [DialogFragment] that presents the user with options to "Edit" or "Delete"
 * a selected profile.
 *
 * This dialog acts as a simple menu. When an option is selected, it either:
 * 1.  Sends a result back to the parent fragment (`ManageProfilesDialogFragment`) to
 * trigger the "edit" flow.
 * 2.  Opens the [DeleteConfirmationDialogFragment] to begin the "delete" flow.
 */
@AndroidEntryPoint
class EditDeleteProfileDialogFragment : DialogFragment() {

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("EditDeleteProfileDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the dialog instance.
     * It retrieves the profile name from arguments to customize the title and sets up
     * the adapter for the "Edit" and "Delete" options.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val profileName = arguments?.getString(ARG_PROFILE_NAME) ?: "Profile"
        val profileId = arguments?.getLong(ARG_PROFILE_ID) ?: -1L
        Logger.i("EditDeleteProfileDialog: onCreateDialog for Profile ID: $profileId, Name: '$profileName'.")

        val options = arrayOf("Edit", "Delete")

        // Create an ArrayAdapter to display the options, ensuring the text is white.
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.WHITE)
                return view
            }
        }

        // Inflate and configure the custom title view.
        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_manage_specific_profile, profileName)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("EditDeleteProfileDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        // Build the AlertDialog using the custom adapter.
        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
            .setAdapter(adapter) { dialog, which ->
                // This block executes when the user clicks an item in the list.
                when (options[which]) {
                    "Edit" -> {
                        Logger.i("EditDeleteProfileDialog: 'Edit' selected for profile ID $profileId.")
                        // Use the Fragment Result API to send the ID of the profile to be edited
                        // back to the parent fragment (ManageProfilesDialogFragment).
                        parentFragmentManager.setFragmentResult(
                            "editProfileRequest",
                            bundleOf("profileId" to profileId)
                        )
                        // Dismiss this dialog after sending the result.
                        dialog.dismiss()
                    }
                    "Delete" -> {
                        Logger.i("EditDeleteProfileDialog: 'Delete' selected for profile ID $profileId.")
                        // Dismiss this dialog first...
                        dialog.dismiss()
                        // ...then show the final confirmation dialog.
                        if (profileId != -1L) {
                            DeleteConfirmationDialogFragment.newInstance(profileId, profileName)
                                .show(parentFragmentManager, DeleteConfirmationDialogFragment.TAG)
                        } else {
                            Logger.e(null, "EditDeleteProfileDialog: 'Delete' selected with an invalid profile ID (-1).")
                        }
                    }
                }
            }
            .create()
    }

    /**
     * A companion object to provide a standardized factory method for creating instances
     * of this fragment.
     */
    companion object {
        const val TAG = "EditDeleteProfileDialogFragment"
        private const val ARG_PROFILE_ID = "profile_id"
        private const val ARG_PROFILE_NAME = "profile_name"

        /**
         * Creates a new instance of [EditDeleteProfileDialogFragment].
         *
         * @param profileId The unique ID of the profile to be managed.
         * @param profileName The name of the profile, used to customize the dialog title.
         * @return A new fragment instance with the required arguments.
         */
        fun newInstance(profileId: Long, profileName: String): EditDeleteProfileDialogFragment {
            val args = Bundle().apply {
                putLong(ARG_PROFILE_ID, profileId)
                putString(ARG_PROFILE_NAME, profileName)
            }
            return EditDeleteProfileDialogFragment().apply {
                arguments = args
            }
        }
    }
}