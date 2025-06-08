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
import com.example.mtglifetracker.data.AppDatabase
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.PlayerLayoutManager
import com.example.mtglifetracker.view.SettingsDialogFragment
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.GameViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.playerDao(), database.gameSettingsDao(), lifecycleScope)
        GameViewModelFactory(repository)
    }

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
            setDynamicLifeTapListener(segment.lifeCounter, index)

            if (index < gameState.players.size) {
                val player = gameState.players[index]
                segment.lifeCounter.text = player.life.toString()

                val isDeltaActive = gameState.activeDeltaPlayers.contains(index)
                val delta = gameState.playerDeltas.getOrNull(index) ?: 0

                // This logic now precisely targets only the segments that need adjustment.
                val layoutParams = segment.deltaCounter.layoutParams as ConstraintLayout.LayoutParams

                val playerCount = gameState.playerCount
                val angle = segment.angle
                val isSidewaysSegment = angle == 90 || angle == -90 || angle == 270

                // The wider bias is needed only when there are 3 segments stacked vertically.
                // This happens on both sides for 6 players, and only on the right side for 5 players.
                val needsWiderBias = (playerCount == 6 && isSidewaysSegment) ||
                        (playerCount == 5 && (angle == -90 || angle == 270))

                if (needsWiderBias) {
                    // Apply wider spacing for the truly crowded segments.
                    layoutParams.horizontalBias = 0.75f
                } else {
                    // All other segments get the default "glued" position.
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
