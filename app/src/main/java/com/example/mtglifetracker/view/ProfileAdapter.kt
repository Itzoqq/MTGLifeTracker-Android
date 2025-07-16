package com.example.mtglifetracker.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger

/**
 * A [ListAdapter] for displaying a list of user-created [Profile] objects.
 *
 * This adapter is used in the [ManageProfilesDialogFragment] to show all saved profiles.
 * It uses a [ProfileDiffCallback] for efficient updates and animations. The primary
 * interaction is a long-click on an item, which triggers a callback to open the
 * edit/delete menu for that profile.
 *
 * @param onProfileClicked A lambda function that is invoked when a profile item is long-clicked.
 * It passes the corresponding [Profile] object to the listener.
 */
class ProfileAdapter(private val onProfileClicked: (Profile) -> Unit) : ListAdapter<Profile, ProfileAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    /**
     * Called when RecyclerView needs a new [ProfileViewHolder] to represent an item.
     * This is where the item's layout is inflated and the ViewHolder is created.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        Logger.d("ProfileAdapter: onCreateViewHolder called for viewType $viewType.")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view).apply {
            // Set a long-click listener on the item view.
            itemView.setOnLongClickListener {
                // Ensure the position is valid before trying to get the item.
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val profile = getItem(adapterPosition)
                    Logger.i("ProfileAdapter: Long-click detected on profile '${profile.nickname}' at position $adapterPosition.")
                    onProfileClicked(profile)
                } else {
                    Logger.w("ProfileAdapter: Long-click detected on a view with no valid adapter position.")
                }
                // Return true to indicate that the long-click event has been consumed
                // and no further action (like a regular click) should be processed.
                true
            }
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [ProfileViewHolder] to reflect the item at the
     * given position.
     */
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        Logger.d("ProfileAdapter: onBindViewHolder called for position $position.")
        holder.bind(getItem(position))
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Cache references to the views within the item layout for efficiency.
        private val colorView: View = itemView.findViewById(R.id.view_profile_color)
        private val nicknameView: TextView = itemView.findViewById(R.id.tv_profile_nickname)

        /**
         * Binds a [Profile] object to this ViewHolder, updating the UI elements.
         *
         * @param profile The profile data to display.
         */
        fun bind(profile: Profile) {
            Logger.d("ProfileViewHolder: Binding profile '${profile.nickname}' at position $adapterPosition.")
            nicknameView.text = profile.nickname
            // If the profile has a color, set the tint of the color dot and make it visible.
            if (profile.color != null) {
                Logger.d("ProfileViewHolder: Setting color dot to ${profile.color}.")
                colorView.background.setTint(profile.color.toColorInt())
                colorView.visibility = View.VISIBLE
            } else {
                // If there's no color, make the dot invisible so it doesn't take up space.
                Logger.d("ProfileViewHolder: Profile has no color. Hiding color dot.")
                colorView.visibility = View.INVISIBLE
            }
        }
    }
}

/**
 * A [DiffUtil.ItemCallback] for calculating the difference between two [Profile] lists.
 * This allows the [ListAdapter] to perform efficient, animated updates when the list changes.
 */
class ProfileDiffCallback : DiffUtil.ItemCallback<Profile>() {
    /**
     * Checks if two items represent the same entity.
     * In this case, we use the unique profile ID.
     */
    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Checks if the contents of two items are the same.
     * This is called only if `areItemsTheSame` returns true. The data class's generated
     * `equals` method will compare all properties (id, nickname, color).
     */
    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem == newItem
    }
}