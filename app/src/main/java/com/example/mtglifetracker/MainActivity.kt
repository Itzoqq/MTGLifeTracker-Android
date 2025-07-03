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
import com.example.mtglifetracker.view.*
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
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

    fun dismissAllDialogs() {
        val fragmentsToDismiss = supportFragmentManager.fragments.filterIsInstance<DialogFragment>()
        fragmentsToDismiss.forEach { it.dismiss() }
    }

    private fun updateUiForNewState(gameState: GameState) {
        if (playerLayoutManager.playerSegments.size != gameState.playerCount) {
            playerLayoutManager.createPlayerLayouts(gameState.playerCount)
            binding.settingsIcon.bringToFront()
            isFirstLoad = true
        }

        val onScreenLives = playerLayoutManager.playerSegments.mapNotNull { segment ->
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
        val isMassUpdate = changedPlayerCount > 1

        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            if (index < gameState.players.size) {
                val player = gameState.players[index]

                segment.updateUI(player, isFirstLoad || isMassUpdate)

                // Reverted to passing the loop index, which is correct.
                setPlayerSegmentListeners(segment, index)
            }
        }

        if (gameState.players.isNotEmpty()) {
            isFirstLoad = false
        }
    }

    private fun setPlayerSegmentListeners(segment: PlayerSegmentView, playerIndex: Int) {
        segment.lifeCounter.onLifeIncreasedListener = { gameViewModel.increaseLife(playerIndex) }
        segment.lifeCounter.onLifeDecreasedListener = { gameViewModel.decreaseLife(playerIndex) }

        segment.onPlayerNameClickListener = { toggleProfilePopup(segment, playerIndex) }
        segment.onUnloadProfileListener = {
            gameViewModel.unloadProfile(playerIndex)
            segment.profilePopupContainer.visibility = View.GONE
        }
        segment.onPlayerCountersClickListener = {
            // Pass the segment's angle to the dialog
            CommanderDamageDialogFragment.newInstance(playerIndex, segment.angle)
                .show(supportFragmentManager, CommanderDamageDialogFragment.TAG)
        }
    }

    private fun toggleProfilePopup(segment: PlayerSegmentView, playerIndex: Int) {
        if (segment.profilePopupContainer.isVisible) {
            segment.profilePopupContainer.visibility = View.GONE
            return
        }

        SingletonIdlingResource.increment()
        lifecycleScope.launch {
            try {
                val (sortedProfiles, availableProfiles) = withContext(Dispatchers.Default) {
                    val allProfiles = profileViewModel.profiles.first()
                    val allUsedProfileIds = gameViewModel.gameState.value.players
                        .mapNotNull { it.profileId }
                        .toSet()

                    val available = allProfiles.filter { profile ->
                        !allUsedProfileIds.contains(profile.id)
                    }
                    Pair(available.sortedBy { it.nickname }, available)
                }

                if (availableProfiles.isEmpty()) {
                    Snackbar.make(binding.mainContainer, "No other profiles available", Snackbar.LENGTH_SHORT).show()
                    return@launch
                }

                val adapter = ProfilePopupAdapter(sortedProfiles) { selectedProfile ->
                    val currentState = gameViewModel.gameState.value
                    val usedProfileIds = currentState.players
                        .mapNotNull { it.profileId }
                        .toSet()

                    if (usedProfileIds.contains(selectedProfile.id)) {
                        Snackbar.make(
                            binding.mainContainer,
                            "'${selectedProfile.nickname}' is already in use.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        gameViewModel.setPlayerProfile(playerIndex, selectedProfile)
                    }
                    segment.profilePopupContainer.visibility = View.GONE
                }
                segment.profilesRecyclerView.adapter = adapter

                if (segment.profilesRecyclerView.itemDecorationCount > 0) {
                    segment.profilesRecyclerView.removeItemDecorationAt(0)
                }

                if (availableProfiles.size > 1) {
                    val divider = DividerItemDecorationExceptLast(this@MainActivity, R.drawable.custom_divider)
                    segment.profilesRecyclerView.addItemDecoration(divider)
                }

                segment.profilePopupContainer.visibility = View.VISIBLE
            } finally {
                if (!SingletonIdlingResource.countingIdlingResource.isIdleNow) {
                    SingletonIdlingResource.decrement()
                }
            }
        }
    }

    private fun setupStaticListeners() {
        binding.settingsIcon.setOnClickListener {
            playerLayoutManager.playerSegments.forEach { segment ->
                segment.profilePopupContainer.visibility = View.GONE
                segment.playerCountersPopupContainer.visibility = View.GONE
            }
            SettingsDialogFragment().show(supportFragmentManager, SettingsDialogFragment.TAG)
        }
    }
}