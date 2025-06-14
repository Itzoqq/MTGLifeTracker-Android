package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditDeleteProfileDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()

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
                        // Launch a coroutine to fetch the profile
                        lifecycleScope.launch {
                            val profile = profileViewModel.getProfile(profileId)
                            if (profile != null) {
                                // Dismiss this dialog after we have the profile
                                dialog.dismiss()
                                // Launch the Create/Edit dialog in edit mode
                                CreateProfileDialogFragment.newInstanceForEdit(profile)
                                    .show(parentFragmentManager, CreateProfileDialogFragment.TAG)
                            } else {
                                dialog.dismiss()
                            }
                        }
                    }
                    "Delete" -> {
                        // Dismiss this dialog first for delete
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