package com.example.mtglifetracker.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Profile

class ProfilePopupAdapter(
    private val profiles: List<Profile>,
    private val onProfileClicked: (Profile) -> Unit
) : RecyclerView.Adapter<ProfilePopupAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_popup, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profiles[position]
        holder.textView.text = profile.nickname
        holder.itemView.setOnClickListener { onProfileClicked(profile) }
    }

    override fun getItemCount() = profiles.size
}