package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.mtglifetracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditDeleteProfileDialogFragment : DialogFragment() {

    // The ViewModel is no longer needed here as this dialog's only job
    // is to pass a result back.

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val profileName = arguments?.getString(ARG_PROFILE_NAME) ?: "Profile"
        val profileId = arguments?.getLong(ARG_PROFILE_ID) ?: -1L
        val options = arrayOf("Edit", "Delete")

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.WHITE)
                return view
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Manage $profileName")
            .setAdapter(adapter) { dialog, which ->
                when (options[which]) {
                    "Edit" -> {
                        // Set the result for the parent fragment to handle. This is the fix.
                        parentFragmentManager.setFragmentResult(
                            "editProfileRequest",
                            bundleOf("profileId" to profileId)
                        )
                        // Dismiss this dialog. The parent will handle the rest.
                        dialog.dismiss()
                    }
                    "Delete" -> {
                        // The delete flow remains the same, as it already shows a separate,
                        // nested confirmation dialog.
                        dialog.dismiss()
                        if (profileId != -1L) {
                            DeleteConfirmationDialogFragment.newInstance(profileId, profileName)
                                .show(parentFragmentManager, DeleteConfirmationDialogFragment.TAG)
                        }
                    }
                }
            }
            .create()
    }

    companion object {
        const val TAG = "EditDeleteProfileDialogFragment"
        private const val ARG_PROFILE_ID = "profile_id"
        private const val ARG_PROFILE_NAME = "profile_name"

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
