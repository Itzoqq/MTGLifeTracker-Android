package com.example.mtglifetracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
        // First, create the player layouts if the player count has changed.
        if (playerLayoutManager.playerSegments.size != gameState.playerCount) {
            playerLayoutManager.createPlayerLayouts(gameState.playerCount)
            binding.mainContainer.addView(binding.settingsIcon)
        }

        // Now, iterate through all the player segments that exist in the layout.
        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            // THE FIX: Attach the listener for this segment immediately.
            // This only depends on the segment's index, not on the player data,
            // so it's safe to do right away.
            setDynamicLifeTapListener(segment.lifeCounter, index)

            // Then, update the UI with player-specific data only if that data exists.
            // This prevents crashes and handles the initial state where the player
            // list may be temporarily empty.
            if (index < gameState.players.size) {
                val player = gameState.players[index]
                segment.lifeCounter.text = player.life.toString()

                val isDeltaActive = gameState.activeDeltaPlayers.contains(index)
                val delta = gameState.playerDeltas.getOrNull(index) ?: 0

                if (isDeltaActive) {
                    segment.deltaCounter.visibility = View.VISIBLE
                    val deltaText = if (delta > 0) "+$delta" else delta.toString()
                    segment.deltaCounter.text = deltaText

                    val colorResId = when {
                        delta > 0 -> R.color.delta_positive
                        delta < 0 -> R.color.delta_negative
                        else -> R.color.white
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