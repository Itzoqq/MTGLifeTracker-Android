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
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * A DialogFragment for creating a new user profile or editing an existing one.
 *
 * This dialog handles user input for a nickname and color selection. It performs
 * validation on the nickname to ensure it meets length and character requirements and
 * checks for uniqueness before saving to the database via the [ProfileViewModel].
 */
@AndroidEntryPoint
class CreateProfileDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var selectedColor: String? = null
    private val colors = listOf(
        "#F44336", // Red
        "#9C27B0", // Purple
        "#2196F3", // Blue
        "#4CAF50", // Green
        "#FFEB3B", // Yellow
        "#FF9800"  // Orange
    )
    private lateinit var colorAdapter: ColorAdapter

    /**
     * Overrides the default onCancel to ensure any related dialogs are also dismissed,
     * preventing UI leaks.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("CreateProfileDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Determine if the dialog is for creating a new profile or editing an existing one.
        val editingProfileId = arguments?.getLong(ARG_EDIT_MODE_ID, -1L) ?: -1L
        val isEditMode = editingProfileId != -1L
        Logger.i("CreateProfileDialog: onCreateDialog. Is in Edit Mode -> $isEditMode")

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_create_profile, FrameLayout(requireContext()), false)

        val nicknameEditText: EditText = view.findViewById(R.id.et_nickname)
        val colorRecyclerView: RecyclerView = view.findViewById(R.id.rv_colors)

        setupRecyclerView(colorRecyclerView)

        // The nickname field is only editable in create mode.
        nicknameEditText.isEnabled = !isEditMode

        // Set up the custom title with a back arrow.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        titleTextView.text = if (isEditMode) getString(R.string.title_edit_profile) else getString(R.string.title_create_profile)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("CreateProfileDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Save", null) // Set to null to override and control dismissal manually.
            .setNegativeButton("Cancel") { dialog, _ ->
                Logger.i("CreateProfileDialog: Cancel button clicked.")
                dialog.cancel()
            }

        // If in edit mode, populate the fields with the existing profile data.
        if (isEditMode) {
            val nickname = arguments?.getString(ARG_EDIT_MODE_NICKNAME) ?: ""
            val color = arguments?.getString(ARG_EDIT_MODE_COLOR)
            Logger.d("CreateProfileDialog: Populating fields for editing Profile ID $editingProfileId.")
            nicknameEditText.setText(nickname)
            selectedColor = color
            colorAdapter.setSelectedColor(selectedColor)
        }

        val dialog = builder.create()

        // Override the save button's click listener to implement custom validation logic.
        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                if (isEditMode) {
                    // In edit mode, we only update the color.
                    Logger.i("CreateProfileDialog: Save button clicked in Edit Mode.")
                    val updatedProfile = Profile(
                        id = editingProfileId,
                        nickname = nicknameEditText.text.toString(),
                        color = selectedColor
                    )
                    profileViewModel.updateProfile(updatedProfile)
                    dialog.dismiss()
                } else {
                    // In create mode, we validate the new nickname before saving.
                    Logger.i("CreateProfileDialog: Save button clicked in Create Mode.")
                    val nickname = nicknameEditText.text.toString().trim()
                    // FIX: This regex now allows for letters, numbers, AND spaces.
                    val isAlphaNumericWithSpaces = nickname.matches("^[a-zA-Z0-9 ]*$".toRegex())
                    Logger.d("CreateProfileDialog: Validating nickname '$nickname'. Length=${nickname.length}, IsAlphaNumericWithSpaces=$isAlphaNumericWithSpaces.")

                    when {
                        nickname.length < 3 -> Snackbar.make(view, "Nickname must be at least 3 characters", Snackbar.LENGTH_SHORT).show()
                        nickname.length > 14 -> Snackbar.make(view, "Nickname must be no more than 14 characters", Snackbar.LENGTH_SHORT).show()
                        !isAlphaNumericWithSpaces -> Snackbar.make(view, "Nickname can only contain letters, numbers, and spaces", Snackbar.LENGTH_SHORT).show()
                        else -> {
                            // Check for uniqueness before saving.
                            lifecycleScope.launch {
                                if (profileViewModel.doesNicknameExist(nickname)) {
                                    Snackbar.make(view, "Nickname already exists.", Snackbar.LENGTH_SHORT).show()
                                } else {
                                    Logger.i("CreateProfileDialog: Validation passed. Adding new profile.")
                                    profileViewModel.addProfile(nickname, selectedColor)
                                    dialog.dismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
        return dialog
    }

    /**
     * Initializes the RecyclerView for color selection.
     */
    private fun setupRecyclerView(recyclerView: RecyclerView) {
        Logger.d("CreateProfileDialog: setting up color picker RecyclerView.")
        colorAdapter = ColorAdapter(colors) { color ->
            Logger.d("CreateProfileDialog: Color selected: $color")
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