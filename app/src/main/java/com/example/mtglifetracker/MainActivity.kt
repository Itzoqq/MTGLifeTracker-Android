package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.util.isColorDark
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.PlayerLayoutManager
import com.example.mtglifetracker.view.ProfilePopupAdapter
import com.example.mtglifetracker.view.RotatableLayout
import com.example.mtglifetracker.view.SettingsDialogFragment
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var playerLayoutManager: PlayerLayoutManager
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerLayoutManager = PlayerLayoutManager(binding.mainContainer, this)

        setupStaticListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameViewModel.gameState.collect { gameState ->
                    updateUiForNewState(gameState)
                }
            }
        }
    }

    private fun updateUiForNewState(gameState: GameState) {
        if (playerLayoutManager.playerSegments.size != gameState.playerCount) {
            playerLayoutManager.createPlayerLayouts(gameState.playerCount)
            // --- THIS IS THE FIX ---
            // The settingsIcon is no longer removed from the container, so we must not re-add it.
            // Instead, we bring it to the front to ensure it's drawn on top of the player segments.
            binding.settingsIcon.bringToFront()
            // --- END OF FIX ---
            isFirstLoad = true
        }

        // --- Final, Robust Reset Detection Logic ---
        val onScreenLives = playerLayoutManager.playerSegments.mapNotNull { segment ->
            // Ensure the segment is actually part of the layout before accessing its view
            if (segment.parent != null) segment.lifeCounter.life else null
        }
        val newLives = gameState.players.map { it.life }
        var changedPlayerCount = 0
        if (onScreenLives.size == newLives.size) {
            for (i in newLives.indices) {
                if (newLives[i] != onScreenLives[i]) {
                    changedPlayerCount++
                }
            }
        }
        // A "mass update" is any event that changes more than one player's life simultaneously.
        val isMassUpdate = changedPlayerCount > 1
        // --- End of Detection Logic ---

        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            segment.lifeCounter.addDismissibleOverlay(segment.profilePopupContainer)
            setDynamicLifeTapListener(segment.lifeCounter, index)

            if (index < gameState.players.size) {
                val player = gameState.players[index]

                segment.playerName.text = player.name
                segment.playerName.setOnClickListener { toggleProfilePopup(segment, index) }

                // Use the no-delta method for the initial load OR for a mass update event.
                if (isFirstLoad || isMassUpdate) {
                    segment.lifeCounter.setLifeAnimate(player.life)
                } else {
                    segment.lifeCounter.life = player.life
                }

                try {
                    if (player.color != null) {
                        val backgroundColor = player.color.toColorInt()
                        segment.setBackgroundColor(backgroundColor)
                        if (isColorDark(backgroundColor)) {
                            segment.playerName.setTextColor(Color.WHITE)
                        } else {
                            segment.playerName.setTextColor(Color.BLACK)
                        }
                    } else {
                        segment.setBackgroundColor(ContextCompat.getColor(this, R.color.default_segment_background))
                        segment.playerName.setTextColor(Color.WHITE)
                    }
                } catch (_: Exception) {
                    segment.setBackgroundColor(ContextCompat.getColor(this, R.color.default_segment_background))
                    segment.playerName.setTextColor(Color.WHITE)
                }

                if (player.profileId != null) {
                    segment.unloadProfileButton.visibility = View.VISIBLE
                    segment.unloadProfileButton.setOnClickListener {
                        gameViewModel.unloadProfile(index)
                    }
                } else {
                    segment.unloadProfileButton.visibility = View.INVISIBLE
                }
            }
        }

        if (gameState.players.isNotEmpty()) {
            isFirstLoad = false
        }
    }

    // itzoqq/mtglifetracker-android/MTGLifeTracker-Android-16e996fdd3c22096a2d4fcfa1a439d172cba95d4/app/src/main/java/com/example/mtglifetracker/MainActivity.kt
    private fun toggleProfilePopup(segment: RotatableLayout, playerIndex: Int) {
        if (segment.profilePopupContainer.isVisible) {
            segment.profilePopupContainer.visibility = View.GONE
            return
        }

        // Tell Espresso to start waiting
        SingletonIdlingResource.increment()
        lifecycleScope.launch {
            try { // Use a try/finally block to guarantee Espresso is notified
                val (sortedProfiles, availableProfiles) = withContext(Dispatchers.Default) {
                    val allProfiles = profileViewModel.profiles.first()
                    val currentPlayerProfileId = gameViewModel.gameState.value.players.getOrNull(playerIndex)?.profileId
                    val usedProfileIdsByOthers = gameViewModel.gameState.value.players
                        .mapNotNull { it.profileId }
                        .toSet()
                        .minus(currentPlayerProfileId)

                    val available = allProfiles.filter { profile ->
                        !usedProfileIdsByOthers.contains(profile.id)
                    }
                    Pair(available.sortedBy { it.nickname }, available)
                }

                if (availableProfiles.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No other profiles available", Toast.LENGTH_SHORT).show()
                    // IMPORTANT: We must decrement here too before returning
                    SingletonIdlingResource.decrement()
                    return@launch
                }

                val adapter = ProfilePopupAdapter(sortedProfiles) { selectedProfile ->
                    gameViewModel.setPlayerProfile(playerIndex, selectedProfile)
                    segment.profilePopupContainer.visibility = View.GONE
                }
                segment.profilesRecyclerView.adapter = adapter

                // --- START OF FINAL LOGIC ---

                val popupParams = segment.profilePopupContainer.layoutParams

                // Step 1: Set a fixed, generous width using our updated dimension.
                popupParams.width = resources.getDimensionPixelSize(R.dimen.profile_popup_width)

                // Step 2: Calculate height based on item count, capped at 5 items.
                val itemHeightDp = 50
                val itemHeightPx = (itemHeightDp * resources.displayMetrics.density).toInt()
                val heightForAllItems = availableProfiles.size * itemHeightPx
                val maxHeightForFiveItems = 5 * itemHeightPx
                popupParams.height = minOf(heightForAllItems, maxHeightForFiveItems)

                // Step 3: Apply the updated layout params.
                segment.profilePopupContainer.layoutParams = popupParams

                // --- END OF FINAL LOGIC ---

                segment.profilePopupContainer.visibility = View.VISIBLE
            } finally {
                // Tell Espresso to stop waiting
                if (!SingletonIdlingResource.countingIdlingResource.isIdleNow) {
                    SingletonIdlingResource.decrement()
                }
            }
        }
    }

    private fun setDynamicLifeTapListener(view: LifeCounterView, playerIndex: Int) {
        view.onLifeIncreasedListener = { gameViewModel.increaseLife(playerIndex) }
        view.onLifeDecreasedListener = { gameViewModel.decreaseLife(playerIndex) }
    }

    private fun setupStaticListeners() {
        binding.settingsIcon.setOnClickListener {
            SettingsDialogFragment().show(supportFragmentManager, SettingsDialogFragment.TAG)
        }
    }
}