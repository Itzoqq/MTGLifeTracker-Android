package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteConfirmationDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val profileId = arguments?.getLong(ARG_PROFILE_ID) ?: -1
        val profileName = arguments?.getString(ARG_PROFILE_NAME) ?: "this profile"

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete '$profileName'?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("DELETE") { _, _ ->
                if (profileId != -1L) {
                    Log.d("ProfileTest", "Delete button clicked for profile ID: $profileId")
                    profileViewModel.deleteProfile(profileId)
                }
            }

        val dialog = builder.create()

        // Set the button text colors after the dialog is created and shown
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.RED)

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(Color.WHITE)
        }

        return dialog
    }

    companion object {
        const val TAG = "DeleteConfirmationDialogFragment"
        private const val ARG_PROFILE_ID = "profile_id"
        private const val ARG_PROFILE_NAME = "profile_name"

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