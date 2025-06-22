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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditDeleteProfileDialogFragment : DialogFragment() {

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as? MainActivity)?.dismissAllDialogs()
    }

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

        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_manage_specific_profile, profileName)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
            .setAdapter(adapter) { dialog, which ->
                when (options[which]) {
                    "Edit" -> {
                        parentFragmentManager.setFragmentResult(
                            "editProfileRequest",
                            bundleOf("profileId" to profileId)
                        )
                        dialog.dismiss()
                    }
                    "Delete" -> {
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