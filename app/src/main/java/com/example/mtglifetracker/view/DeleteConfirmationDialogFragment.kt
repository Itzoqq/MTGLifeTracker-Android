package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
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

        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_delete_profile)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
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