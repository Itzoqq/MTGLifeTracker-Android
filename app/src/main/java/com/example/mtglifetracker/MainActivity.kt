package com.example.mtglifetracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.data.GamePreferences
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.PlayerLayoutManager
import com.example.mtglifetracker.view.SettingsDialogFragment
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.GameViewModelFactory
import kotlinx.coroutines.launch

/**
 * The main and only activity in the application. It is responsible for observing state
 * from the [GameViewModel] and rendering the appropriate UI for the current game state.
 * It delegates the creation of complex player layouts to the [PlayerLayoutManager].
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels {
        val preferences = GamePreferences(applicationContext)
        val repository = GameRepository(preferences)
        GameViewModelFactory(repository)
    }

    private lateinit var playerLayoutManager: PlayerLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the layout manager
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

    /**
     * Updates the entire UI based on a new [GameState] from the ViewModel.
     * This function is the single point of entry for all UI rendering.
     * @param gameState The new state to be rendered.
     */
    private fun updateUiForNewState(gameState: GameState) {
        // If the player count has changed, rebuild the layout
        if (playerLayoutManager.playerSegments.size != gameState.playerCount) {
            playerLayoutManager.createPlayerLayouts(gameState.playerCount)
            // Re-add the settings icon to the container after it's cleared
            binding.mainContainer.addView(binding.settingsIcon)
        }

        // Update the life and delta for each player segment
        playerLayoutManager.playerSegments.forEachIndexed { index, segment ->
            if (index < gameState.players.size) {
                val player = gameState.players[index]
                segment.lifeCounter.text = player.life.toString()
                setDynamicLifeTapListener(segment.lifeCounter, index)

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

    /**
     * Sets the click listeners for a specific [LifeCounterView].
     * This function connects a UI component to a specific player index in the ViewModel.
     * @param view The [LifeCounterView] to attach the listener to.
     * @param playerIndex The index of the player this view represents.
     */
    private fun setDynamicLifeTapListener(view: LifeCounterView, playerIndex: Int) {
        view.onLifeIncreasedListener = { gameViewModel.increaseLife(playerIndex) }
        view.onLifeDecreasedListener = { gameViewModel.decreaseLife(playerIndex) }
    }

    /**
     * Sets listeners for UI elements that are always present and do not change,
     * such as the main settings icon.
     */
    private fun setupStaticListeners() {
        binding.settingsIcon.setOnClickListener {
            SettingsDialogFragment().show(supportFragmentManager, SettingsDialogFragment.TAG)
        }
    }
}