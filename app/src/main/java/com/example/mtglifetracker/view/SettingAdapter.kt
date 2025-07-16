package com.example.mtglifetracker.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.util.Logger

/**
 * A RecyclerView adapter for displaying the main list of options in the settings menu.
 *
 * This adapter takes a simple array of strings and displays them as a clickable list.
 * When an item is clicked, it invokes a callback with the position of the clicked item,
 * allowing the hosting fragment ([SettingsDialogFragment]) to launch the appropriate sub-dialog.
 *
 * @param options An array of strings representing the setting options to be displayed.
 * @param onItemClicked A callback lambda that is invoked with the integer position of the
 * item that the user clicked.
 */
class SettingsAdapter(
    private val options: Array<String>,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    /**
     * A ViewHolder that describes an item view and its contents.
     * For this simple adapter, it just holds a reference to a single TextView.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] to represent an item.
     * This is where the item's layout is inflated from XML.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Logger.d("SettingsAdapter: onCreateViewHolder called for viewType $viewType.")
        // Use Android's built-in simple list item layout.
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method sets the text for the item and configures its click listener.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val optionText = options[position]
        Logger.d("SettingsAdapter: Binding option '$optionText' to position $position.")

        holder.textView.text = optionText
        // Ensure the text color is white to match the custom dialog theme.
        holder.textView.setTextColor(Color.WHITE)

        // Set the click listener for the entire item view.
        holder.itemView.setOnClickListener {
            Logger.i("SettingsAdapter: Item clicked at position $position ('$optionText').")
            // Invoke the callback with the clicked position.
            onItemClicked(position)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount() = options.size
}