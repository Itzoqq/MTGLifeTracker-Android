package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mtglifetracker.R

// Renamed as requested
class EditDeleteProfileDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val profileName = arguments?.getString(ARG_PROFILE_NAME) ?: "Profile"
        val options = arrayOf("Edit", "Delete")

        // Create a custom adapter to set the text color of the items to white.
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Get the default list item view
                val view = super.getView(position, convertView, parent)
                // Find the TextView within the list item and set its color to white
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.WHITE)
                return view
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Manage profile: $profileName")
            // Use setAdapter instead of setItems to apply the custom text color
            .setAdapter(adapter) { dialog, which ->
                when (options[which]) {
                    "Edit" -> {
                        Toast.makeText(requireContext(), "Edit clicked (not implemented)", Toast.LENGTH_SHORT).show()
                    }
                    "Delete" -> {
                        Toast.makeText(requireContext(), "Delete clicked (not implemented)", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            // The negative button is removed as requested.
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