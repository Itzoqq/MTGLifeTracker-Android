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
 * A [DialogFragment] for creating a new user profile or editing an existing one.
 *
 * This dialog's behavior changes based on the arguments it receives. If launched with
 * profile data, it enters "edit" mode, pre-filling the fields and updating an existing
 * profile on save. If launched without arguments, it enters "create" mode, allowing for
 * the creation of a new profile with validation for the nickname.
 */
@AndroidEntryPoint
class CreateProfileDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var selectedColor: String? = null
    private val colors = listOf(
        "#F44336", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800"
    )
    private lateinit var colorAdapter: ColorAdapter

    /**
     * Overridden to ensure that if the user cancels the dialog (e.g., by pressing the
     * back button), all other dialogs are dismissed as well, returning to a clean state.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("CreateProfileDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Builds and returns the dialog instance.
     * This method sets up the view, determines if it's in "create" or "edit" mode,
     * populates data for "edit" mode, and configures the save button's validation logic.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editingProfileId = arguments?.getLong(ARG_EDIT_MODE_ID, -1L) ?: -1L
        val isEditMode = editingProfileId != -1L
        Logger.i("CreateProfileDialog: onCreateDialog. Is in Edit Mode -> $isEditMode")

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Inflate view with a temporary parent to resolve layout parameter warnings.
        val view = inflater.inflate(R.layout.dialog_create_profile, FrameLayout(requireContext()), false)
        val nicknameEditText: EditText = view.findViewById(R.id.et_nickname)
        val colorRecyclerView: RecyclerView = view.findViewById(R.id.rv_colors)

        setupRecyclerView(colorRecyclerView)

        // The nickname field is always enabled initially. It will be disabled below if in edit mode.
        nicknameEditText.isEnabled = true

        // Inflate and set up the custom title view.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        titleTextView.text = if (isEditMode) getString(R.string.title_edit_profile) else getString(R.string.title_create_profile)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("CreateProfileDialog: Back arrow in custom title clicked. Dismissing.")
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Save", null) // Set to null to override behavior and prevent auto-dismiss.
            .setNegativeButton("Cancel") { dialog, _ ->
                Logger.i("CreateProfileDialog: Cancel button clicked.")
                dialog.cancel()
            }

        // If in edit mode, populate the fields with the existing profile's data.
        if (isEditMode) {
            val nickname = arguments?.getString(ARG_EDIT_MODE_NICKNAME) ?: ""
            val color = arguments?.getString(ARG_EDIT_MODE_COLOR)
            Logger.d("CreateProfileDialog: Populating fields for editing Profile ID $editingProfileId with Nickname='$nickname', Color='$color'.")

            nicknameEditText.setText(nickname)
            // Disable the nickname field in edit mode to prevent changing it.
            nicknameEditText.isEnabled = false

            selectedColor = color
            colorAdapter.setSelectedColor(selectedColor)
        }

        val dialog = builder.create()

        // We override the button's click listener here to add our custom validation logic.
        // This prevents the dialog from closing if the input is invalid.
        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                if (isEditMode) {
                    // --- SAVE LOGIC FOR EDIT MODE ---
                    Logger.i("CreateProfileDialog: Save button clicked in Edit Mode.")
                    val updatedProfile = Profile(
                        id = editingProfileId,
                        nickname = nicknameEditText.text.toString(), // Nickname is disabled but we still read it.
                        color = selectedColor
                    )
                    profileViewModel.updateProfile(updatedProfile)
                    dialog.dismiss()
                } else {
                    // --- SAVE LOGIC FOR CREATE MODE ---
                    Logger.i("CreateProfileDialog: Save button clicked in Create Mode.")
                    val nickname = nicknameEditText.text.toString().trim()
                    val isAlphaNumeric = nickname.matches("^[a-zA-Z0-9]*$".toRegex())
                    Logger.d("CreateProfileDialog: Validating nickname '$nickname'. Length=${nickname.length}, IsAlphaNumeric=$isAlphaNumeric.")

                    // Perform a series of validation checks.
                    when {
                        nickname.length < 3 -> {
                            Logger.w("CreateProfileDialog: Validation failed - nickname too short.")
                            Snackbar.make(view, "Nickname must be at least 3 characters", Snackbar.LENGTH_SHORT).show()
                        }
                        nickname.length > 14 -> {
                            Logger.w("CreateProfileDialog: Validation failed - nickname too long.")
                            Snackbar.make(view, "Nickname must be no more than 14 characters", Snackbar.LENGTH_SHORT).show()
                        }
                        !isAlphaNumeric -> {
                            Logger.w("CreateProfileDialog: Validation failed - nickname not alphanumeric.")
                            Snackbar.make(view, "Nickname can only contain letters and numbers", Snackbar.LENGTH_SHORT).show()
                        }
                        else -> {
                            // If basic validation passes, check for uniqueness in the database.
                            lifecycleScope.launch {
                                Logger.d("CreateProfileDialog: Checking if nickname '$nickname' already exists.")
                                if (profileViewModel.doesNicknameExist(nickname)) {
                                    Logger.w("CreateProfileDialog: Validation failed - nickname already exists.")
                                    Snackbar.make(view, "Nickname already exists.", Snackbar.LENGTH_SHORT).show()
                                } else {
                                    // All validation passed, add the profile and dismiss the dialog.
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
     * Sets up the [RecyclerView] for displaying color swatches.
     * @param recyclerView The RecyclerView to be configured.
     */
    private fun setupRecyclerView(recyclerView: RecyclerView) {
        Logger.d("CreateProfileDialog: setting up color picker RecyclerView.")
        colorAdapter = ColorAdapter(colors) { color ->
            Logger.d("CreateProfileDialog: Color selected: $color")
            selectedColor = color
        }
        recyclerView.adapter = colorAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 6) // Display colors in a grid.
    }

    /**
     * A companion object to provide standardized factory methods for creating fragment instances.
     */
    companion object {
        const val TAG = "CreateProfileDialogFragment"
        private const val ARG_EDIT_MODE_ID = "edit_mode_id"
        private const val ARG_EDIT_MODE_NICKNAME = "edit_mode_nickname"
        private const val ARG_EDIT_MODE_COLOR = "edit_mode_color"

        /**
         * Creates a new instance of [CreateProfileDialogFragment] pre-configured for "edit" mode.
         *
         * @param profile The [Profile] to be edited.
         * @return A new fragment instance with the profile's data packed into its arguments.
         */
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