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
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateProfileDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private var selectedColor: String? = null
    private val colorSwatches = mutableListOf<View>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_create_profile, null)

        val nicknameEditText: EditText = view.findViewById(R.id.et_nickname)
        val colorGrid: GridLayout = view.findViewById(R.id.grid_colors)
        setupColorGrid(colorGrid)

        builder.setView(view)
            .setTitle("Create Profile")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val nickname = nicknameEditText.text.toString().trim()
                if (nickname.length < 3) {
                    Toast.makeText(requireContext(), "Nickname must be at least 3 characters", Toast.LENGTH_SHORT).show()
                } else {
                    // Launch a coroutine to perform the database check
                    lifecycleScope.launch {
                        if (profileViewModel.doesNicknameExist(nickname)) {
                            // If it exists, show an error Toast
                            Toast.makeText(requireContext(), "Nickname already exists.", Toast.LENGTH_SHORT).show()
                        } else {
                            // If it doesn't exist, add the profile and close the dialog
                            profileViewModel.addProfile(nickname, selectedColor)
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        return dialog
    }

    private fun setupColorGrid(grid: GridLayout) {
        val colors = listOf(
            "#F44336", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800"
        )

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
                setOnClickListener {
                    selectColor(this, colorString)
                }
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
    }
}