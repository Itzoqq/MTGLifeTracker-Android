package com.example.mtglifetracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.util.Logger // Make sure you have created this Logger object
import com.example.mtglifetracker.view.CommanderDamageDialogFragment
import com.example.mtglifetracker.view.DividerItemDecorationExceptLast
import com.example.mtglifetracker.view.PlayerLayoutManager
import com.example.mtglifetracker.view.PlayerSegmentView
import com.example.mtglifetracker.view.ProfilePopupAdapter
import com.example.mtglifetracker.view.SettingsDialogFragment
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The main and only activity for the application, responsible for hosting all UI components.
 *
 * This activity observes the [GameViewModel] for state changes and updates the UI accordingly.
 * It manages the lifecycle of player segments using [PlayerLayoutManager] and handles the
 * display of all dialog fragments for settings and player interactions. It serves as the central
 * hub for user interaction and UI state management.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var playerLayoutManager: PlayerLayoutManager
    private var isFirstLoad = true

    /**
     * Called when the activity is first created.
     * This is the entry point for the activity's lifecycle. It is responsible for inflating the layout,
     * initializing the [PlayerLayoutManager], setting up static listeners that don't change,
     * and starting the main coroutine to observe and react to game state changes from the ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the default action bar as we use a custom UI
        supportActionBar?.hide()
        Logger.i("MainActivity.onCreate: Activity is being created.")

        // Inflate the activity's layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Logger.d("MainActivity.onCreate: View binding complete and content view set.")

        // Initialize the manager responsible for creating and arranging player UI segments
        playerLayoutManager = PlayerLayoutManager(binding.mainContainer, this)

        // Set up listeners for persistent UI elements like the settings icon
        setupStaticListeners()

        // Launch a lifecycle-aware coroutine to collect game state updates
        lifecycleScope.launch {
            // repeatOnLifecycle ensures the collector is only active when the Activity is STARTED
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Logger.i("MainActivity.onCreate: Lifecycle is STARTED. Starting to collect game state.")
                gameViewModel.gameState.collect { gameState ->
                    // This block will execute every time the game state emits a new value
                    updateUiForNewState(gameState)
                }
            }
        }
    }

    /**
     * Finds and dismisses all currently visible [DialogFragment] instances.
     * This is a utility function to clean up the UI, for example, before showing a new dialog
     * or after a global action like a game reset, ensuring no stale dialogs remain.
     */
    fun dismissAllDialogs() {
        // Filter the list of all fragments managed by the FragmentManager to find DialogFragments
        val fragmentsToDismiss = supportFragmentManager.fragments.filterIsInstance<DialogFragment>()
        Logger.i("Dismissing all active dialogs. Found ${fragmentsToDismiss.size} dialog(s).")
        fragmentsToDismiss.forEach {
            Logger.d("Dismissing dialog: ${it.javaClass.simpleName}")
            it.dismiss()
        }
    }

    /**
     * The core UI update function that is called whenever the [GameState] changes.
     * It intelligently handles creating/removing player segments if the player count changes,
     * and updates each existing segment's UI (life, name, commander damage) with the new data.
     *
     * @param gameState The new, immutable state of the game to be rendered.
     */
    private fun updateUiForNewState(gameState: GameState) {
        Logger.d("updateUiForNewState: Received new game state. Player count: ${gameState.playerCount}, Players in state: ${gameState.players.size}")

        // Check if the number of player segments on screen matches the new player count
        if (playerLayoutManager.playerSegments.size != gameState.playerCount) {
            Logger.i("updateUiForNewState: Player count mismatch. Current: ${playerLayoutManager.playerSegments.size}, New: ${gameState.playerCount}. Recreating player layouts.")
            playerLayoutManager.createPlayerLayouts(gameState.playerCount)
            // Ensure the settings icon is always drawn on top of the player segments
            binding.settingsIcon.bringToFront()
            // Flag that this is a fresh layout creation
            isFirstLoad = true
        }

        // A "mass update" is detected if all players' life totals are equal to the starting life.
        // This typically happens on a game reset or first load. It allows us to use smoother animations
        // for setting the initial life total, rather than treating it as a delta change.
        val isMassUpdate = gameState.players.isNotEmpty() && gameState.players.all { it.life == gameState.startingLife }
        Logger.d("updateUiForNewState: isMassUpdate = $isMassUpdate, isFirstLoad = $isFirstLoad")

        // Iterate through each player segment view and update it with the corresponding player's data
        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            if (index < gameState.players.size) {
                val player = gameState.players[index]
                Logger.d("updateUiForNewState: Updating segment for player $index. Name: ${player.name}, Life: ${player.life}, ProfileID: ${player.profileId}")

                // Delegate the actual UI update to the PlayerSegmentView itself
                segment.updateUI(
                    player,
                    gameState.players,
                    gameState.allCommanderDamage,
                    isFirstLoad || isMassUpdate // Pass the flag to determine animation style
                )

                // Re-apply listeners to ensure they are connected to the correct player index and view model actions
                setPlayerSegmentListeners(segment, index)
            } else {
                Logger.w("updateUiForNewState: No player data found for segment index $index. This should not happen if player count matches.")
            }
        }

        // After the first successful UI update with player data, set the flag to false
        if (gameState.players.isNotEmpty()) {
            if (isFirstLoad) Logger.i("updateUiForNewState: First load complete.")
            isFirstLoad = false
        }
    }

    /**
     * Sets or re-sets the various listeners for a specific [PlayerSegmentView].
     * This is crucial because views can be recycled or recreated. This method ensures that
     * interactions like life changes, name clicks (for profile selection), and commander damage
     * icon clicks are always wired to the correct player and ViewModel functions.
     *
     * @param segment The [PlayerSegmentView] to attach listeners to.
     * @param playerIndex The 0-based index of the player this segment represents.
     */
    private fun setPlayerSegmentListeners(segment: PlayerSegmentView, playerIndex: Int) {
        Logger.d("setPlayerSegmentListeners: Setting listeners for player segment $playerIndex.")
        // Listen for life increase/decrease events from the LifeCounterView
        segment.lifeCounter.onLifeIncreasedListener = { gameViewModel.increaseLife(playerIndex) }
        segment.lifeCounter.onLifeDecreasedListener = { gameViewModel.decreaseLife(playerIndex) }

        // Listen for a click on the player's name to open the profile selection popup
        segment.onPlayerNameClickListener = { toggleProfilePopup(segment, playerIndex) }
        // Listen for a long-press on the player's name to unload the currently assigned profile
        segment.onUnloadProfileListener = {
            Logger.i("setPlayerSegmentListeners: Unload profile requested for player $playerIndex.")
            gameViewModel.unloadProfile(playerIndex)
            segment.profilePopupContainer.visibility = View.GONE
        }
        // Listen for a click on the commander damage summary to open the damage tracking dialog
        segment.onPlayerCountersClickListener = {
            Logger.i("setPlayerSegmentListeners: Commander damage dialog requested for player $playerIndex.")
            // Pass the segment's angle to the dialog for correct layout orientation
            CommanderDamageDialogFragment.newInstance(playerIndex, segment.angle)
                .show(supportFragmentManager, CommanderDamageDialogFragment.TAG)
        }
    }

    /**
     * Shows or hides the profile selection popup for a given player segment.
     * This function fetches the list of available (unassigned) profiles from the ViewModel
     * and displays them in a RecyclerView within the popup. If no profiles are available,
     * it shows a [Snackbar] message instead.
     *
     * @param segment The segment where the popup should be displayed.
     * @param playerIndex The index of the player initiating the action.
     */
    private fun toggleProfilePopup(segment: PlayerSegmentView, playerIndex: Int) {
        // If the popup is already visible, hide it and do nothing else.
        if (segment.profilePopupContainer.isVisible) {
            Logger.d("toggleProfilePopup: Closing profile popup for player $playerIndex.")
            segment.profilePopupContainer.visibility = View.GONE
            return
        }
        Logger.i("toggleProfilePopup: Opening profile popup for player $playerIndex.")

        // Increment idling resource for Espresso tests to wait for the data fetch
        SingletonIdlingResource.increment()
        lifecycleScope.launch {
            try {
                // Fetch and process profile data on a background thread
                val (sortedProfiles, availableProfiles) = withContext(Dispatchers.Default) {
                    val allProfiles = profileViewModel.profiles.first()
                    val allUsedProfileIds = gameViewModel.gameState.value.players
                        .mapNotNull { it.profileId }
                        .toSet()
                    Logger.d("toggleProfilePopup: Found ${allProfiles.size} total profiles and ${allUsedProfileIds.size} used profiles.")

                    // Filter out profiles that are already assigned to a player
                    val available = allProfiles.filter { profile -> !allUsedProfileIds.contains(profile.id) }
                    Logger.d("toggleProfilePopup: ${available.size} profiles are available for assignment.")
                    // Return a sorted pair for the UI
                    Pair(available.sortedBy { it.nickname }, available)
                }

                // If no profiles are available to assign, show a message and exit.
                if (availableProfiles.isEmpty()) {
                    Logger.w("toggleProfilePopup: No available profiles to show for player $playerIndex.")
                    Snackbar.make(binding.mainContainer, "No other profiles available", Snackbar.LENGTH_SHORT).show()
                    return@launch
                }

                // Create the adapter for the RecyclerView
                val adapter = ProfilePopupAdapter(sortedProfiles) { selectedProfile ->
                    val currentState = gameViewModel.gameState.value
                    val usedProfileIds = currentState.players.mapNotNull { it.profileId }.toSet()
                    Logger.d("toggleProfilePopup: Profile '${selectedProfile.nickname}' (ID: ${selectedProfile.id}) selected for player $playerIndex.")

                    // Final check to prevent race conditions where another player assigns the profile
                    if (usedProfileIds.contains(selectedProfile.id)) {
                        Logger.w("toggleProfilePopup: Stale UI click. Profile '${selectedProfile.nickname}' is already in use.")
                        Snackbar.make(binding.mainContainer, "'${selectedProfile.nickname}' is already in use.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        // If the profile is still available, assign it
                        Logger.i("toggleProfilePopup: Assigning profile '${selectedProfile.nickname}' to player $playerIndex.")
                        gameViewModel.setPlayerProfile(playerIndex, selectedProfile)
                    }
                    // Hide the popup after selection
                    segment.profilePopupContainer.visibility = View.GONE
                }
                segment.profilesRecyclerView.adapter = adapter

                // Manage item dividers: remove any existing one, then add one back if there's more than one item
                if (segment.profilesRecyclerView.itemDecorationCount > 0) {
                    segment.profilesRecyclerView.removeItemDecorationAt(0)
                }
                if (availableProfiles.size > 1) {
                    val divider = DividerItemDecorationExceptLast(this@MainActivity, R.drawable.custom_divider)
                    segment.profilesRecyclerView.addItemDecoration(divider)
                }

                // Make the popup visible to the user
                segment.profilePopupContainer.visibility = View.VISIBLE
            } finally {
                // Decrement idling resource to signal that the async operation is complete
                if (!SingletonIdlingResource.countingIdlingResource.isIdleNow) {
                    SingletonIdlingResource.decrement()
                }
            }
        }
    }

    /**
     * Sets up listeners for UI elements that are static and do not depend on the game state.
     * This is done once in `onCreate` to avoid redundant listener setup.
     */
    private fun setupStaticListeners() {
        binding.settingsIcon.setOnClickListener {
            Logger.i("setupStaticListeners: Settings icon clicked, opening settings dialog.")
            // Hide any open popups before showing the main settings dialog
            playerLayoutManager.playerSegments.forEach { segment ->
                segment.profilePopupContainer.visibility = View.GONE
                segment.playerCountersPopupContainer.visibility = View.GONE
            }
            SettingsDialogFragment().show(supportFragmentManager, SettingsDialogFragment.TAG)
        }
        Logger.d("setupStaticListeners: Static listeners have been set up.")
    }
}