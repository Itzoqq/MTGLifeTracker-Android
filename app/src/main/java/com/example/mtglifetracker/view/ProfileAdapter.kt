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

// Accept a click listener in the constructor
class ProfileAdapter(private val onProfileClicked: (Profile) -> Unit) : ListAdapter<Profile, ProfileAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view).apply {
            // Set the click listener on the item view itself
            itemView.setOnClickListener {
                // Ensure the position is valid
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // Trigger the lambda with the clicked profile
                    onProfileClicked(getItem(adapterPosition))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.view_profile_color)
        private val nicknameView: TextView = itemView.findViewById(R.id.tv_profile_nickname)

        fun bind(profile: Profile) {
            nicknameView.text = profile.nickname
            if (profile.color != null) {
                colorView.background.setTint(profile.color.toColorInt())
                colorView.visibility = View.VISIBLE
            } else {
                colorView.visibility = View.INVISIBLE
            }
        }
    }
}

class ProfileDiffCallback : DiffUtil.ItemCallback<Profile>() {
    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem == newItem
    }
}