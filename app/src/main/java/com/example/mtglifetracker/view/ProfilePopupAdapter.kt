package com.example.mtglifetracker.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile

class ProfilePopupAdapter(
    private val profiles: List<Profile>,
    private val onProfileClicked: (Profile) -> Unit
) : RecyclerView.Adapter<ProfilePopupAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
        // Add a reference to the color view
        val colorView: View = view.findViewById(R.id.view_popup_profile_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // We use our updated list_item_popup.xml layout here
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_popup, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profiles[position]
        holder.textView.text = profile.nickname
        holder.itemView.setOnClickListener { onProfileClicked(profile) }

        // Logic to show, hide, and set the color of the swatch
        if (profile.color != null) {
            holder.colorView.background.setTint(profile.color.toColorInt())
            holder.colorView.visibility = View.VISIBLE
        } else {
            // Use GONE to ensure it takes up no space if there's no color
            holder.colorView.visibility = View.GONE
        }
    }

    override fun getItemCount() = profiles.size
}