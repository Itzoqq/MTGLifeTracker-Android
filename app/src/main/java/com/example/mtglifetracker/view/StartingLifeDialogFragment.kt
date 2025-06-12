package com.example.mtglifetracker.view

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.GameViewModel

class StartingLifeDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val lifeOptions = arrayOf("20", "30", "40", "Custom") // Added "30"

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, lifeOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Starting Life")
            .setAdapter(adapter) { _, which ->
                when (lifeOptions[which]) {
                    "20" -> gameViewModel.changeStartingLife(20)
                    "30" -> gameViewModel.changeStartingLife(30)
                    "40" -> gameViewModel.changeStartingLife(40)
                    "Custom" -> {
                        CustomLifeDialogFragment().show(parentFragmentManager, CustomLifeDialogFragment.TAG)
                    }
                }
            }
            .create()
    }

    companion object {
        const val TAG = "StartingLifeDialogFragment"
    }
}