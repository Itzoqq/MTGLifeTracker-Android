package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class PlayerCountDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playerCountOptions = arrayOf("2", "3", "4", "5", "6")

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, playerCountOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        // Inflate the custom title view, providing a temporary parent to resolve warnings
        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)

        // Set the title from string resources and set the back arrow's click listener
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_player_count)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
            .setAdapter(adapter) { _, which ->
                val defaultPlayerCount = gameViewModel.gameState.value.playerCount
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: defaultPlayerCount

                gameViewModel.changePlayerCount(selectedPlayerCount)
            }
            .create()
    }

    companion object {
        const val TAG = "PlayerCountDialogFragment"
    }
}