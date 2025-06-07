package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.data.GamePreferences
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.view.RotatableLayout
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.GameViewModelFactory
import kotlinx.coroutines.launch

/**
 * The main and only activity in the application. It is responsible for observing state
 * from the [GameViewModel] and rendering the appropriate UI for the current game state.
 * It does not contain any game logic itself.
 */
class MainActivity : AppCompatActivity() {

    /** The auto-generated binding object for the activity's layout (activity_main.xml). */
    private lateinit var binding: ActivityMainBinding

    /**
     * The ViewModel instance for this activity.
     * It is created using a custom factory to provide the [GameRepository] dependency.
     */
    private val gameViewModel: GameViewModel by viewModels {
        val preferences = GamePreferences(applicationContext)
        val repository = GameRepository(preferences)
        GameViewModelFactory(repository)
    }

    /**
     * A map that holds a reference to the UI components for each layout type.
     * The Key is the player count (e.g., 4) and the Value is a list of the [RotatableLayout]
     * segments for that specific layout. This avoids repetitive logic in the UI update function.
     */
    private lateinit var playerUiMap: Map<Int, List<RotatableLayout>>

    /** A simple list of all the main layout containers to make hiding them easier. */
    private lateinit var allLayoutContainers: List<ConstraintLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Inflate the layout using View Binding and set it as the content view.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Build the data structures that map player counts to their UI components.
        setupUiMappings()
        // Set listeners for static UI elements that are always present.
        setupStaticListeners()

        // Launch a coroutine that observes the game state from the ViewModel.
        // repeatOnLifecycle ensures the collector is only active when the Activity is started.
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
        allLayoutContainers.forEach { it.visibility = View.GONE }

        val activePlayerSegments = playerUiMap[gameState.playerCount] ?: return

        (activePlayerSegments.first().parent as? View)?.visibility = View.VISIBLE

        activePlayerSegments.forEachIndexed { index, segment ->
            if (index < gameState.players.size) {
                val player = gameState.players[index]
                segment.lifeCounter.text = player.life.toString()
                setDynamicLifeTapListener(segment.lifeCounter, index)

                // NEW LOGIC: Check if the player's delta sequence is active.
                val isDeltaActive = gameState.activeDeltaPlayers.contains(index)
                val delta = gameState.playerDeltas.getOrNull(index) ?: 0

                if (isDeltaActive) {
                    // If the sequence is active, the counter is ALWAYS visible, even at 0.
                    segment.deltaCounter.visibility = View.VISIBLE
                    val deltaText = if (delta > 0) "+$delta" else delta.toString()
                    segment.deltaCounter.text = deltaText
                } else {
                    // If the sequence is not active (i.e., timed out), hide the counter.
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
        view.onLifeIncreasedListener = {
            gameViewModel.increaseLife(playerIndex)
        }
        view.onLifeDecreasedListener = {
            gameViewModel.decreaseLife(playerIndex)
        }
    }

    /**
     * Populates the [playerUiMap] and [allLayoutContainers] data structures once.
     * This method centralizes all view references to keep the rest of the code clean.
     */
    private fun setupUiMappings() {
        // Create a map that links a player count to a list of that layout's RotatableLayout segments.
        playerUiMap = mapOf(
            2 to listOf(
                binding.twoPlayerLayout.player1Segment,
                binding.twoPlayerLayout.player2Segment
            ),
            3 to listOf(
                binding.threePlayerLayout.player1Segment,
                binding.threePlayerLayout.player2Segment,
                binding.threePlayerLayout.player3Segment
            ),
            4 to listOf(
                binding.fourPlayerLayout.player1Segment,
                binding.fourPlayerLayout.player2Segment,
                binding.fourPlayerLayout.player3Segment,
                binding.fourPlayerLayout.player4Segment
            ),
            5 to listOf(
                binding.fivePlayerLayout.player1Segment,
                binding.fivePlayerLayout.player2Segment,
                binding.fivePlayerLayout.player3Segment,
                binding.fivePlayerLayout.player4Segment,
                binding.fivePlayerLayout.player5Segment
            ),
            6 to listOf(
                binding.sixPlayerLayout.player1Segment,
                binding.sixPlayerLayout.player2Segment,
                binding.sixPlayerLayout.player3Segment,
                binding.sixPlayerLayout.player4Segment,
                binding.sixPlayerLayout.player5Segment,
                binding.sixPlayerLayout.player6Segment
            )
        )

        // Create a simple list of all parent containers for easy hiding/showing.
        allLayoutContainers = listOf(
            binding.twoPlayerLayout.root,
            binding.threePlayerLayout.root,
            binding.fourPlayerLayout.root,
            binding.fivePlayerLayout.root,
            binding.sixPlayerLayout.root
        )
    }

    /**
     * Sets listeners for UI elements that are always present and do not change,
     * such as the main settings icon.
     */
    private fun setupStaticListeners() {
        binding.settingsIcon.setOnClickListener {
            showSettingsPopup()
        }
    }

    /**
     * Builds and displays the main settings dialog popup.
     */
    private fun showSettingsPopup() {
        val settingsOptions = arrayOf("Number of Players")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingsOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Settings")
            .setAdapter(adapter) { dialog, which ->
                if (which == 0) showPlayerCountSelection()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    /**
     * Displays the sub-dialog for selecting the number of players.
     */
    private fun showPlayerCountSelection() {
        val playerCountOptions = arrayOf("2", "3", "4", "5", "6")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playerCountOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.WHITE)
                return view
            }
        }

        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Number of Players")
            .setAdapter(adapter) { dialog, which ->
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: gameViewModel.gameState.value.playerCount
                gameViewModel.changePlayerCount(selectedPlayerCount)
                dialog.dismiss()
            }
            .create()
            .show()
    }
}