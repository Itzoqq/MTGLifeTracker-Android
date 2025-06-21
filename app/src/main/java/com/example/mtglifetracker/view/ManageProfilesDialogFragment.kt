package com.example.mtglifetracker.view

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.mtglifetracker.SingletonIdlingResource

@AndroidEntryPoint
class ManageProfilesDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_manage_profiles, null)

        recyclerView = view.findViewById(R.id.rv_profiles)
        emptyTextView = view.findViewById(R.id.tv_empty_profiles)

        // --- Limit the height of the RecyclerView ---
        val screenHeight = resources.displayMetrics.heightPixels
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = screenHeight / 2
        recyclerView.layoutParams = layoutParams

        profileAdapter = ProfileAdapter { profile ->
            EditDeleteProfileDialogFragment.newInstance(profile.id, profile.nickname)
                .show(parentFragmentManager, EditDeleteProfileDialogFragment.TAG)
        }

        recyclerView.adapter = profileAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up the listener for the result from EditDeleteProfileDialogFragment.
        // This is the core of the fix.
        parentFragmentManager.setFragmentResultListener("editProfileRequest", this) { _, bundle ->
            val profileId = bundle.getLong("profileId", -1L)
            if (profileId != -1L) {
                // Launch a coroutine to fetch the full profile object before showing the next dialog.
                lifecycleScope.launch {
                    val profile = profileViewModel.getProfile(profileId)
                    profile?.let {
                        // Now it's safe to show the edit dialog.
                        CreateProfileDialogFragment.newInstanceForEdit(it)
                            .show(parentFragmentManager, CreateProfileDialogFragment.TAG)
                    }
                }
            }
        }

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_profile)
            .setOnClickListener {
                CreateProfileDialogFragment().show(parentFragmentManager, CreateProfileDialogFragment.TAG)
            }

        builder.setView(view)
            .setTitle("Manage Profiles")

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        // Start observing profiles when the dialog becomes visible
        observeProfiles()
    }

    private fun observeProfiles() {
        lifecycleScope.launch {
            profileViewModel.profiles.collect { profiles ->
                Log.d("ProfileTest", "ManageProfilesDialog - Profile list updated. Count: ${profiles.size}")

                // Tell Espresso the app is now busy
                SingletonIdlingResource.increment()
                try {
                    profileAdapter.submitList(profiles.toList()) {
                        // This callback runs after the list has been diffed and submitted.
                        updateVisibility(profiles.isEmpty())
                    }
                } finally {
                    // This will now ALWAYS be called, even if submitList fails.
                    SingletonIdlingResource.decrement()
                }
            }
        }
    }

    private fun updateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            Log.d("ProfileTest", "ManageProfilesDialog - Showing empty text view.")
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        } else {
            Log.d("ProfileTest", "ManageProfilesDialog - Showing profiles list.")
            recyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "ManageProfilesDialogFragment"
    }
}
