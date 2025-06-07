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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gameViewModel: GameViewModel by viewModels {
        val preferences = GamePreferences(applicationContext)
        val repository = GameRepository(preferences)
        GameViewModelFactory(repository)
    }

    // This map will hold a reference to the UI components for each layout type.
    // The Key is the player count (Int), the Value is a List of the player segments.
    private lateinit var playerUiMap: Map<Int, List<RotatableLayout>>
    private lateinit var allLayoutContainers: List<ConstraintLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize our data structures that hold references to all the UI components.
        setupUiMappings()

        binding.settingsIcon.setOnClickListener {
            showSettingsPopup()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameViewModel.gameState.collect { gameState ->
                    updateUiForNewState(gameState)
                }
            }
        }
    }

    /**
     * This function is now much simpler. It uses the pre-built map to find the
     * correct UI components for the current player count and then applies the state.
     */
    private fun updateUiForNewState(gameState: GameState) {
        // First, hide all layout containers
        allLayoutContainers.forEach { it.visibility = View.GONE }

        // Find the list of player segments for the current player count
        val activePlayerSegments = playerUiMap[gameState.playerCount] ?: return

        // Show the parent container of the active segments
        (activePlayerSegments.first().parent as? View)?.visibility = View.VISIBLE

        // Loop through the active segments and players to set text and listeners.
        // This logic is now generic and works for any player count.
        activePlayerSegments.forEachIndexed { index, segment ->
            if (index < gameState.players.size) {
                segment.lifeCounter.text = gameState.players[index].life.toString()
                setLifeTapListener(segment.lifeCounter, index)
            }
        }
    }

    /**
     * Helper function to set listeners on a LifeCounterView.
     * This remains unchanged.
     */
    private fun setLifeTapListener(view: LifeCounterView, playerIndex: Int) {
        view.onLifeIncreasedListener = {
            gameViewModel.increaseLife(playerIndex)
        }
        view.onLifeDecreasedListener = {
            gameViewModel.decreaseLife(playerIndex)
        }
    }

    /**
     * This new function populates our maps once, centralizing all view references.
     * This is where the boilerplate now lives, but it's organized and runs only one time.
     */
    private fun setupUiMappings() {
        // Create a map that links a player count to a list of that layout's segments.
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