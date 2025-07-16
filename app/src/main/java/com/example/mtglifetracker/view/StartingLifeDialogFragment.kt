package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
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
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.GameViewModel

/**
 * A [DialogFragment] that allows the user to select a starting life total.
 *
 * This dialog presents a list of common starting life totals (20, 30, 40) as well as
 * a "Custom" option. Selecting a preset value immediately updates the [GameViewModel].
 * Selecting "Custom" launches the [CustomLifeDialogFragment] for manual input.
 */
class StartingLifeDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("StartingLifeDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the dialog instance.
     * This method sets up the list of life total options, the adapter to display them,
     * and the click listener to handle the user's selection.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("StartingLifeDialog: onCreateDialog called.")
        val lifeOptions = arrayOf("20", "30", "40", "Custom")

        // Create a custom ArrayAdapter to ensure the text color is white.
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, lifeOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        // Inflate the custom title view.
        val inflater = requireActivity().layoutInflater
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)

        // Set the title from string resources and configure the back arrow's click listener.
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_starting_life)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("StartingLifeDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        // Build the AlertDialog, setting the custom title and adapter.
        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCustomTitle(customTitleView)
            .setAdapter(adapter) { _, which ->
                // This block executes when the user selects an item from the list.
                val selection = lifeOptions[which]
                Logger.i("StartingLifeDialog: User selected option '$selection'.")
                when (selection) {
                    "20" -> gameViewModel.changeStartingLife(20)
                    "30" -> gameViewModel.changeStartingLife(30)
                    "40" -> gameViewModel.changeStartingLife(40)
                    "Custom" -> {
                        Logger.d("StartingLifeDialog: 'Custom' selected. Launching CustomLifeDialogFragment.")
                        CustomLifeDialogFragment().show(parentFragmentManager, CustomLifeDialogFragment.TAG)
                    }
                }
            }
            .create()
    }

    /**
     * A companion object to hold constants related to the fragment.
     */
    companion object {
        // A unique tag for identifying this fragment in the FragmentManager.
        const val TAG = "StartingLifeDialogFragment"
    }
}