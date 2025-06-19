package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.PlayerLayoutManager
import com.example.mtglifetracker.view.ProfilePopupAdapter
import com.example.mtglifetracker.view.RotatableLayout
import com.example.mtglifetracker.view.SettingsDialogFragment
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import com.example.mtglifetracker.util.isColorDark

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var playerLayoutManager: PlayerLayoutManager

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
            binding.mainContainer.addView(binding.settingsIcon)
        }

        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            // Make sure any open popups are dismissed when the player touches the life counter
            segment.lifeCounter.addDismissableOverlay(segment.profilePopupContainer)
            setDynamicLifeTapListener(segment.lifeCounter, index)

            if (index < gameState.players.size) {
                val player = gameState.players[index]

                // Set Player Name
                segment.playerName.text = player.name

                // Set Click Listener for Name
                segment.playerName.setOnClickListener {
                    toggleProfilePopup(segment, index)
                }

                // Set Life Total
                segment.lifeCounter.text = player.life.toString()

                // Set Background and Text Color
                try {
                    if (player.color != null) {
                        val backgroundColor = player.color.toColorInt()
                        segment.setBackgroundColor(backgroundColor)
                        // Set text color for high contrast
                        if (isColorDark(backgroundColor)) {
                            segment.playerName.setTextColor(Color.WHITE)
                        } else {
                            segment.playerName.setTextColor(Color.BLACK)
                        }
                    } else {
                        // Set default background and text color
                        segment.setBackgroundColor(ContextCompat.getColor(this, R.color.default_segment_background))
                        segment.playerName.setTextColor(Color.WHITE)
                    }
                } catch (_: Exception) {
                    // Fallback to default colors in case of an error
                    segment.setBackgroundColor(ContextCompat.getColor(this, R.color.default_segment_background))
                    segment.playerName.setTextColor(Color.WHITE)
                }

                // Control Unload Button Visibility
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
    }

    private fun toggleProfilePopup(segment: RotatableLayout, playerIndex: Int) {
        // If the popup is already visible, hide it and do nothing else.
        if (segment.profilePopupContainer.isVisible) {
            segment.profilePopupContainer.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val allProfiles = profileViewModel.profiles.first()

            val currentPlayerProfileId = gameViewModel.gameState.value.players.getOrNull(playerIndex)?.profileId
            val usedProfileIdsByOthers = gameViewModel.gameState.value.players
                .mapNotNull { it.profileId }
                .toSet()
                .minus(currentPlayerProfileId)

            val availableProfiles = allProfiles.filter { profile ->
                !usedProfileIdsByOthers.contains(profile.id)
            }

            if (availableProfiles.isEmpty()) {
                Toast.makeText(this@MainActivity, "No other profiles available", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val sortedProfiles = availableProfiles.sortedBy { it.nickname }

            val adapter = ProfilePopupAdapter(sortedProfiles) { selectedProfile ->
                gameViewModel.setPlayerProfile(playerIndex, selectedProfile)
                segment.profilePopupContainer.visibility = View.GONE
            }
            segment.profilesRecyclerView.adapter = adapter

            // --- Dynamic Sizing Logic for Popup ---
            val recyclerParams = segment.profilesRecyclerView.layoutParams
            val isSideways = segment.angle == 90 || segment.angle == -90

            if (isSideways) {
                recyclerParams.width = (segment.height * 0.9).toInt()
                recyclerParams.height = (segment.width * 0.85).toInt()
            } else {
                recyclerParams.width = resources.getDimensionPixelSize(R.dimen.profile_popup_width)
                recyclerParams.height = (segment.height * 0.8).toInt()
            }
            segment.profilesRecyclerView.layoutParams = recyclerParams

            segment.profilePopupContainer.visibility = View.VISIBLE
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