package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateProfileDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var selectedColor: String? = null
    private val colors = listOf(
        "#F44336", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800"
    )
    private lateinit var colorAdapter: ColorAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editingProfileId = arguments?.getLong(ARG_EDIT_MODE_ID, -1L)
        val isEditMode = editingProfileId != null && editingProfileId != -1L

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_create_profile, null)

        val nicknameEditText: EditText = view.findViewById(R.id.et_nickname)
        val colorRecyclerView: RecyclerView = view.findViewById(R.id.rv_colors)

        setupRecyclerView(colorRecyclerView)

        nicknameEditText.isEnabled = true

        builder.setView(view)
            .setTitle(if (isEditMode) "Edit Profile" else "Create Profile")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        if (isEditMode) {
            val nickname = arguments?.getString(ARG_EDIT_MODE_NICKNAME) ?: ""
            val color = arguments?.getString(ARG_EDIT_MODE_COLOR)

            nicknameEditText.setText(nickname)
            nicknameEditText.isEnabled = false // Keep nickname non-editable in edit mode

            selectedColor = color
            colorAdapter.setSelectedColor(selectedColor)
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                if (isEditMode) {
                    val updatedProfile = Profile(
                        id = editingProfileId,
                        nickname = nicknameEditText.text.toString(),
                        color = selectedColor
                    )
                    profileViewModel.updateProfile(updatedProfile)
                    dialog.dismiss()
                } else {
                    val nickname = nicknameEditText.text.toString().trim()
                    if (nickname.length < 3) {
                        Toast.makeText(requireContext(), "Nickname must be at least 3 characters", Toast.LENGTH_SHORT).show()
                    } else {
                        lifecycleScope.launch {
                            if (profileViewModel.doesNicknameExist(nickname)) {
                                Toast.makeText(requireContext(), "Nickname already exists.", Toast.LENGTH_SHORT).show()
                            } else {
                                profileViewModel.addProfile(nickname, selectedColor)
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
        return dialog
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        colorAdapter = ColorAdapter(colors) { color ->
            selectedColor = color
        }
        recyclerView.adapter = colorAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 6) // Display 6 colors per row
    }

    companion object {
        const val TAG = "CreateProfileDialogFragment"
        private const val ARG_EDIT_MODE_ID = "edit_mode_id"
        private const val ARG_EDIT_MODE_NICKNAME = "edit_mode_nickname"
        private const val ARG_EDIT_MODE_COLOR = "edit_mode_color"

        fun newInstanceForEdit(profile: Profile): CreateProfileDialogFragment {
            val args = Bundle().apply {
                putLong(ARG_EDIT_MODE_ID, profile.id)
                putString(ARG_EDIT_MODE_NICKNAME, profile.nickname)
                profile.color?.let { putString(ARG_EDIT_MODE_COLOR, it) }
            }
            return CreateProfileDialogFragment().apply {
                arguments = args
            }
        }
    }
}