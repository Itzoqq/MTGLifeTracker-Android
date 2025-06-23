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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
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

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editingProfileId = arguments?.getLong(ARG_EDIT_MODE_ID, -1L)
        val isEditMode = editingProfileId != null && editingProfileId != -1L

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Inflate view with a temporary parent to resolve layout params
        val view = inflater.inflate(R.layout.dialog_create_profile, FrameLayout(requireContext()), false)
        val nicknameEditText: EditText = view.findViewById(R.id.et_nickname)
        val colorRecyclerView: RecyclerView = view.findViewById(R.id.rv_colors)

        setupRecyclerView(colorRecyclerView)

        nicknameEditText.isEnabled = true

        // Inflate and set up the custom title
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        titleTextView.text = if (isEditMode) getString(R.string.title_edit_profile) else getString(R.string.title_create_profile)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        if (isEditMode) {
            val nickname = arguments?.getString(ARG_EDIT_MODE_NICKNAME) ?: ""
            val color = arguments?.getString(ARG_EDIT_MODE_COLOR)

            nicknameEditText.setText(nickname)
            nicknameEditText.isEnabled = false

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
                    // --- NEW AND UPDATED VALIDATION LOGIC ---
                    val isAlphaNumeric = nickname.matches("^[a-zA-Z0-9]*$".toRegex())

                    when {
                        nickname.length < 3 -> {
                            Snackbar.make(view, "Nickname must be at least 3 characters", Snackbar.LENGTH_SHORT).show()
                        }
                        nickname.length > 14 -> {
                            Snackbar.make(view, "Nickname must be no more than 14 characters", Snackbar.LENGTH_SHORT).show()
                        }
                        !isAlphaNumeric -> {
                            Snackbar.make(view, "Nickname can only contain letters and numbers", Snackbar.LENGTH_SHORT).show()
                        }
                        else -> {
                            lifecycleScope.launch {
                                if (profileViewModel.doesNicknameExist(nickname)) {
                                    Snackbar.make(view, "Nickname already exists.", Snackbar.LENGTH_SHORT).show()
                                } else {
                                    profileViewModel.addProfile(nickname, selectedColor)
                                    dialog.dismiss()
                                }
                            }
                        }
                    }
                    // --- END OF VALIDATION LOGIC ---
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
        recyclerView.layoutManager = GridLayoutManager(context, 6)
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