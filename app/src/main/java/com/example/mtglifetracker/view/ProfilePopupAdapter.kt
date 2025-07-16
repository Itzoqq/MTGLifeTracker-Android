package com.example.mtglifetracker.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger

/**
 * A RecyclerView adapter for displaying a list of assignable profiles in a popup menu.
 *
 * This adapter is used within the [PlayerSegmentView] to show a list of available profiles
 * that can be assigned to a player. It displays each profile's nickname and a colored dot
 * if a color is associated with it.
 *
 * @param profiles The list of [Profile] objects to be displayed.
 * @param onProfileClicked A callback lambda that is invoked when a profile is selected from the list.
 */
class ProfilePopupAdapter(
    private val profiles: List<Profile>,
    private val onProfileClicked: (Profile) -> Unit
) : RecyclerView.Adapter<ProfilePopupAdapter.ViewHolder>() {

    /**
     * A ViewHolder that describes an item view and its contents.
     * It caches references to the UI elements within the item layout for efficient access.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // A reference to the TextView that displays the profile's nickname.
        val textView: TextView = view.findViewById(android.R.id.text1)
        // A reference to the View used as a color swatch.
        val colorView: View = view.findViewById(R.id.view_popup_profile_color)
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] to represent an item.
     * This is where the item's layout is inflated.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Logger.d("ProfilePopupAdapter: onCreateViewHolder called for viewType $viewType.")
        // Inflate the custom layout for a single item in the popup list.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_popup, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [ViewHolder] to reflect the profile at the
     * given position.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profiles[position]
        Logger.d("ProfilePopupAdapter: Binding profile '${profile.nickname}' to position $position.")
        holder.textView.text = profile.nickname

        // Set a click listener for the entire item view.
        holder.itemView.setOnClickListener {
            Logger.i("ProfilePopupAdapter: Profile '${profile.nickname}' clicked at position $position.")
            onProfileClicked(profile)
        }

        // Set the visibility and color of the color swatch based on the profile's data.
        if (profile.color != null) {
            Logger.d("ProfilePopupAdapter: Profile has color ${profile.color}. Setting swatch tint.")
            // The background is a shape drawable, so we can set its tint.
            holder.colorView.background.setTint(profile.color.toColorInt())
            holder.colorView.visibility = View.VISIBLE
        } else {
            Logger.d("ProfilePopupAdapter: Profile has no color. Hiding swatch.")
            // Use GONE to ensure the view takes up no space in the layout.
            holder.colorView.visibility = View.GONE
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount() = profiles.size
}