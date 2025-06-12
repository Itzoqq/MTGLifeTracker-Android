package com.example.mtglifetracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.PlayerLayoutManager
import com.example.mtglifetracker.view.SettingsDialogFragment
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels()

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

    private fun updateUiForNewState(GameState: GameState) {
        if (playerLayoutManager.playerSegments.size != GameState.playerCount) {
            playerLayoutManager.createPlayerLayouts(GameState.playerCount)
            binding.mainContainer.addView(binding.settingsIcon)
        }

        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            // Clear any previous overlays and register the new one.
            // This prevents issues when the layout is rebuilt.
            segment.lifeCounter.clearDismissableOverlays()
            segment.lifeCounter.addDismissableOverlay(segment.playerSettingsPopup)

            // The icon now toggles the visibility of the in-layout popup.
            segment.playerSettingsIcon.setOnClickListener {
                val popup = segment.playerSettingsPopup
                popup.visibility = if (popup.isVisible) View.GONE else View.VISIBLE
            }

            setDynamicLifeTapListener(segment.lifeCounter, index)

            if (index < GameState.players.size) {
                val player = GameState.players[index]
                segment.lifeCounter.text = player.life.toString()

                val isDeltaActive = GameState.activeDeltaPlayers.contains(index)
                val delta = GameState.playerDeltas.getOrNull(index) ?: 0

                val layoutParams = segment.deltaCounter.layoutParams as ConstraintLayout.LayoutParams

                val playerCount = GameState.playerCount
                val angle = segment.angle
                val isSidewaysSegment = angle == 90 || angle == -90 || angle == 270

                val needsWiderBias = (playerCount == 6 && isSidewaysSegment) ||
                        (playerCount == 5 && (angle == -90 || angle == 270))

                if (needsWiderBias) {
                    layoutParams.horizontalBias = 0.75f
                } else {
                    layoutParams.horizontalBias = 0.65f
                }
                segment.deltaCounter.layoutParams = layoutParams

                if (isDeltaActive) {
                    segment.deltaCounter.visibility = View.VISIBLE
                    val deltaText = if (delta > 0) "+$delta" else delta.toString()
                    segment.deltaCounter.text = deltaText

                    val colorResId = when {
                        delta > 0 -> R.color.delta_positive
                        delta < 0 -> R.color.delta_negative
                        else -> R.color.white // Should not happen if delta is not 0
                    }
                    segment.deltaCounter.setTextColor(ContextCompat.getColor(this, colorResId))
                } else {
                    segment.deltaCounter.visibility = View.GONE
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