package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageProfilesDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_manage_profiles, null)

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_profiles)
        val emptyTextView: TextView = view.findViewById(R.id.tv_empty_profiles)
        val profileAdapter = ProfileAdapter()

        recyclerView.adapter = profileAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // *** THE FIX: Changed viewLifecycleOwner.lifecycleScope to just lifecycleScope ***
        lifecycleScope.launch {
            profileViewModel.profiles.collect { profiles ->
                profileAdapter.submitList(profiles)
                if (profiles.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyTextView.visibility = View.GONE
                }
            }
        }

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_profile)
            .setOnClickListener {
                CreateProfileDialogFragment().show(parentFragmentManager, CreateProfileDialogFragment.TAG)
            }

        builder.setView(view)
            .setTitle("Manage Profiles")
            .setNegativeButton("Close") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }

    companion object {
        const val TAG = "ManageProfilesDialogFragment"
    }
}