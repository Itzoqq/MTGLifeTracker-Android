package com.example.mtglifetracker.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Matrix
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mtglifetracker.R

class PlayerCountersDialogFragment : DialogFragment() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_player_counters, null)

        // Get arguments passed from MainActivity
        val nickname = arguments?.getString(ARG_NICKNAME) ?: "Player"
        val rotation = arguments?.getFloat(ARG_ROTATION) ?: 0f
        val playerCount = arguments?.getInt(ARG_PLAYER_COUNT) ?: 0

        // Find the grid layout and add the counter boxes
        val countersGrid = view.findViewById<GridLayout>(R.id.counters_grid)
        countersGrid.removeAllViews() // Clear any old views first
        (0 until playerCount).forEach { i ->
            val counterBox = inflater.inflate(R.layout.item_counter_box, countersGrid, false)
            countersGrid.addView(counterBox)
        }

        // Set up the custom title
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.tv_dialog_title)
        val closeButton = customTitleView.findViewById<ImageView>(R.id.iv_back_arrow)

        titleTextView.text = getString(R.string.player_counters_title, nickname)
        closeButton.visibility = View.GONE // Hide the 'X' button

        builder.setCustomTitle(customTitleView)
            .setView(view)

        val dialog = builder.create()

        dialog.setOnShowListener {
            val window = dialog.window ?: return@setOnShowListener
            val decorView = window.decorView

            // Apply the visual rotation
            decorView.rotation = rotation

            // Set the touch listener to intercept and transform touch events
            decorView.setOnTouchListener { v, event ->
                val matrix = Matrix()

                // Calculate the inverse matrix to "un-rotate" the touch coordinates
                when (rotation) {
                    90f -> {
                        matrix.setRotate(-90f)
                        matrix.postTranslate(0f, v.height.toFloat())
                    }
                    -90f, 270f -> {
                        matrix.setRotate(90f)
                        matrix.postTranslate(v.width.toFloat(), 0f)
                    }
                    180f -> {
                        matrix.setRotate(-180f)
                        matrix.postTranslate(v.width.toFloat(), v.height.toFloat())
                    }
                }

                // Apply the transformation to the event
                event.transform(matrix)

                // Return false to allow the system to continue processing the now-corrected event
                false
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // Make the dialog a square based on screen width for consistent rotation
            val displayMetrics = requireContext().resources.displayMetrics
            val size = (displayMetrics.widthPixels * 0.85).toInt()
            window.setLayout(size, size)
            window.setGravity(Gravity.CENTER)
        }
    }

    companion object {
        const val TAG = "PlayerCountersDialogFragment"
        private const val ARG_NICKNAME = "nickname"
        private const val ARG_ROTATION = "rotation"
        private const val ARG_PLAYER_COUNT = "player_count"

        fun newInstance(nickname: String, rotation: Float, playerCount: Int): PlayerCountersDialogFragment {
            val args = Bundle().apply {
                putString(ARG_NICKNAME, nickname)
                putFloat(ARG_ROTATION, rotation)
                putInt(ARG_PLAYER_COUNT, playerCount)
            }
            return PlayerCountersDialogFragment().apply {
                arguments = args
            }
        }
    }
}