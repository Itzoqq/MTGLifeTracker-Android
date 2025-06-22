package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.SingletonIdlingResource
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageProfilesDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        // Inflate view with a temporary parent to resolve layout params
        val view = inflater.inflate(R.layout.dialog_manage_profiles, FrameLayout(requireContext()), false)

        recyclerView = view.findViewById(R.id.rv_profiles)
        emptyTextView = view.findViewById(R.id.tv_empty_profiles)

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

        parentFragmentManager.setFragmentResultListener("editProfileRequest", this) { _, bundle ->
            val profileId = bundle.getLong("profileId", -1L)
            if (profileId != -1L) {
                lifecycleScope.launch {
                    val profile = profileViewModel.getProfile(profileId)
                    profile?.let {
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

        // Inflate and set up the custom title
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_manage_profiles)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { dismiss() }

        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        observeProfiles()
    }

    private fun observeProfiles() {
        lifecycleScope.launch {
            profileViewModel.profiles.collect { profiles ->
                Log.d("ProfileTest", "ManageProfilesDialog - Profile list updated. Count: ${profiles.size}")
                SingletonIdlingResource.increment()
                try {
                    profileAdapter.submitList(profiles.toList()) {
                        updateVisibility(profiles.isEmpty())
                    }
                } finally {
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