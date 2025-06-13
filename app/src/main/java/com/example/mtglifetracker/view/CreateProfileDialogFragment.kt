package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateProfileDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private var selectedColor: String? = null
    private val colorSwatches = mutableListOf<View>()
    private val colors = listOf(
        "#F44336", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800"
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editingProfileId = arguments?.getLong(ARG_EDIT_MODE_ID, -1L)

        // --- THIS IS THE FIX ---
        // This check is now safer and correctly handles all cases.
        val isEditMode = editingProfileId != null && editingProfileId != -1L

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_create_profile, null)

        val nicknameEditText: EditText = view.findViewById(R.id.et_nickname)
        val colorGrid: GridLayout = view.findViewById(R.id.grid_colors)
        setupColorGrid(colorGrid)

        nicknameEditText.isEnabled = true

        builder.setView(view)
            .setTitle(if (isEditMode) "Edit Profile" else "Create Profile")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        if (isEditMode) {
            val nickname = arguments?.getString(ARG_EDIT_MODE_NICKNAME) ?: ""
            val color = arguments?.getString(ARG_EDIT_MODE_COLOR)

            nicknameEditText.setText(nickname)
            nicknameEditText.isEnabled = false

            color?.let { savedColor ->
                val colorIndex = colors.indexOf(savedColor)
                if (colorIndex != -1) {
                    val swatchToSelect = colorSwatches.getOrNull(colorIndex)
                    swatchToSelect?.let { selectColor(it, savedColor) }
                } else {
                    selectedColor = savedColor
                }
            }
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                if (isEditMode) {
                    val updatedProfile = Profile(
                        id = editingProfileId, // This is now safe
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

    private fun setupColorGrid(grid: GridLayout) {
        colors.forEach { colorString ->
            val swatch = View(requireContext()).apply {
                val swatchSize = resources.getDimensionPixelSize(R.dimen.color_swatch_size)
                val params = GridLayout.LayoutParams().apply {
                    width = swatchSize
                    height = swatchSize
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = params
                background = createColorSwatch(colorString)
                setOnClickListener { selectColor(this, colorString) }
            }
            colorSwatches.add(swatch)
            grid.addView(swatch)
        }
    }

    private fun createColorSwatch(colorString: String): StateListDrawable {
        val color = colorString.toColorInt()
        val selectedStrokeWidth = resources.getDimensionPixelSize(R.dimen.selected_stroke_width)
        val defaultStrokeWidth = resources.getDimensionPixelSize(R.dimen.default_stroke_width)
        val selectedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(selectedStrokeWidth, Color.WHITE)
        }
        val defaultDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(defaultStrokeWidth, Color.GRAY)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_selected), selectedDrawable)
            addState(intArrayOf(), defaultDrawable)
        }
    }

    private fun selectColor(selectedView: View, colorString: String) {
        if (selectedColor == colorString) {
            selectedView.isSelected = false
            selectedColor = null
        } else {
            colorSwatches.forEach { it.isSelected = false }
            selectedView.isSelected = true
            selectedColor = colorString
        }
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