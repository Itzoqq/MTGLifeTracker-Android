package com.example.mtglifetracker.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
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
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * A [DialogFragment] that serves as the main interface for managing user profiles.
 *
 * This dialog displays a list of all saved profiles in a [RecyclerView]. It provides
 * functionality to:
 * - Add a new profile via a Floating Action Button, which launches [CreateProfileDialogFragment].
 * - Edit or delete an existing profile by long-pressing an item, which launches [EditDeleteProfileDialogFragment].
 * - Display a message when no profiles have been created yet.
 */
@AndroidEntryPoint
class ManageProfilesDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private var dividerItemDecoration: DividerItemDecorationExceptLast? = null

    /**
     * Called when the dialog is canceled. This ensures all other open dialogs are also
     * dismissed, providing a clean navigation flow back to the main screen.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.i("ManageProfilesDialog: onCancel called.")
        (activity as? MainActivity)?.dismissAllDialogs()
    }

    /**
     * Creates and configures the dialog instance.
     * This method inflates the layout, sets up the RecyclerView, its adapter, the custom title,
     * and the listeners for adding and editing profiles.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.i("ManageProfilesDialog: onCreateDialog called.")
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val inflater = requireActivity().layoutInflater

        val view = inflater.inflate(R.layout.dialog_manage_profiles, FrameLayout(requireContext()), false)

        recyclerView = view.findViewById(R.id.rv_profiles)
        emptyTextView = view.findViewById(R.id.tv_empty_profiles)

        // Set a max height for the RecyclerView to prevent it from taking up the whole screen.
        val screenHeight = resources.displayMetrics.heightPixels
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = screenHeight / 2
        recyclerView.layoutParams = layoutParams
        Logger.d("ManageProfilesDialog: RecyclerView height constrained to ${layoutParams.height}px.")

        // The adapter's callback is triggered on a long-press, opening the Edit/Delete menu.
        profileAdapter = ProfileAdapter { profile ->
            Logger.i("ManageProfilesDialog: Profile '${profile.nickname}' long-pressed. Opening Edit/Delete dialog.")
            EditDeleteProfileDialogFragment.newInstance(profile.id, profile.nickname)
                .show(parentFragmentManager, EditDeleteProfileDialogFragment.TAG)
        }

        recyclerView.adapter = profileAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Create an instance of our custom item decoration.
        dividerItemDecoration = DividerItemDecorationExceptLast(requireContext(), R.drawable.custom_divider)

        // Set up a listener to receive results from other fragments, specifically for the "Edit" action.
        parentFragmentManager.setFragmentResultListener("editProfileRequest", this) { _, bundle ->
            val profileId = bundle.getLong("profileId", -1L)
            Logger.i("ManageProfilesDialog: Received fragment result 'editProfileRequest' for profile ID $profileId.")
            if (profileId != -1L) {
                lifecycleScope.launch {
                    val profile = profileViewModel.getProfile(profileId)
                    profile?.let {
                        Logger.d("ManageProfilesDialog: Found profile to edit. Launching CreateProfileDialog in edit mode.")
                        CreateProfileDialogFragment.newInstanceForEdit(it)
                            .show(parentFragmentManager, CreateProfileDialogFragment.TAG)
                    } ?: Logger.e(null, "ManageProfilesDialog: Could not find profile with ID $profileId to edit.")
                }
            }
        }

        // Set the listener for the "add profile" button.
        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_profile)
            .setOnClickListener {
                Logger.i("ManageProfilesDialog: Add profile FAB clicked. Launching CreateProfileDialog.")
                CreateProfileDialogFragment().show(parentFragmentManager, CreateProfileDialogFragment.TAG)
            }

        // Configure the custom title view.
        val customTitleView = inflater.inflate(R.layout.dialog_custom_title, FrameLayout(requireContext()), false)
        customTitleView.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.title_manage_profiles)
        customTitleView.findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener {
            Logger.d("ManageProfilesDialog: Back arrow clicked. Dismissing.")
            dismiss()
        }

        builder.setCustomTitle(customTitleView)
            .setView(view)

        return builder.create()
    }

    /**
     * Called when the fragment is becoming visible to the user.
     * We start observing the profile list here to ensure the UI is always up-to-date.
     */
    override fun onResume() {
        super.onResume()
        Logger.d("ManageProfilesDialog: onResume called. Starting to observe profiles.")
        observeProfiles()
    }

    /**
     * Sets up a coroutine to collect the list of profiles from the ViewModel's Flow.
     * When a new list is emitted, it updates the adapter and manages the visibility of
     * the item dividers and the "empty list" message.
     */
    private fun observeProfiles() {
        lifecycleScope.launch {
            profileViewModel.profiles.collect { profiles ->
                Logger.i("ManageProfilesDialog: Profiles flow emitted a new list with ${profiles.size} item(s).")
                // Use idling resource to help with UI testing synchronization.
                SingletonIdlingResource.increment()
                try {
                    val sortedProfiles = profiles.sortedBy { it.nickname }

                    // Logic to add or remove the item divider. It's only shown if there are 2 or more items.
                    dividerItemDecoration?.let { recyclerView.removeItemDecoration(it) }
                    if (sortedProfiles.size > 1) {
                        Logger.d("ManageProfilesDialog: More than one profile. Adding item decoration.")
                        dividerItemDecoration?.let { recyclerView.addItemDecoration(it) }
                    } else {
                        Logger.d("ManageProfilesDialog: One or zero profiles. No item decoration needed.")
                    }

                    // Submit the new list to the adapter. DiffUtil will handle animations.
                    profileAdapter.submitList(sortedProfiles) {
                        // This callback runs after the list has been displayed.
                        Logger.d("ManageProfilesDialog: Profile list submitted and diffing complete.")
                        updateVisibility(sortedProfiles.isEmpty())
                    }
                } finally {
                    SingletonIdlingResource.decrement()
                }
            }
        }
    }

    /**
     * Toggles the visibility of the RecyclerView and the "empty list" message.
     * @param isEmpty True if the list of profiles is empty, false otherwise.
     */
    private fun updateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            Logger.d("ManageProfilesDialog: List is empty. Showing empty text view.")
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        } else {
            Logger.d("ManageProfilesDialog: List is not empty. Showing profiles RecyclerView.")
            recyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "ManageProfilesDialogFragment"
    }
}