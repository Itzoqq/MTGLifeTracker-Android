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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mtglifetracker.data.GamePreferences
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.databinding.ActivityMainBinding
import com.example.mtglifetracker.view.LifeCounterView
import com.example.mtglifetracker.viewmodel.GameState
import com.example.mtglifetracker.viewmodel.GameViewModel
import com.example.mtglifetracker.viewmodel.GameViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // The ViewModel is now created using our custom factory
    private val gameViewModel: GameViewModel by viewModels {
        // Manually create our dependencies here
        val preferences = GamePreferences(applicationContext)
        val repository = GameRepository(preferences)
        GameViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun updateUiForNewState(gameState: GameState) {
        // This function remains the same as before
        binding.twoPlayerLayout.root.visibility = if (gameState.playerCount == 2) View.VISIBLE else View.GONE
        binding.threePlayerLayout.root.visibility = if (gameState.playerCount == 3) View.VISIBLE else View.GONE
        binding.fourPlayerLayout.root.visibility = if (gameState.playerCount == 4) View.VISIBLE else View.GONE
        binding.fivePlayerLayout.root.visibility = if (gameState.playerCount == 5) View.VISIBLE else View.GONE
        binding.sixPlayerLayout.root.visibility = if (gameState.playerCount == 6) View.VISIBLE else View.GONE

        when (gameState.playerCount) {
            2 -> {
                val (p1, p2) = gameState.players
                binding.twoPlayerLayout.lifeCounterTextP1TwoPlayer.text = p1.life.toString()
                binding.twoPlayerLayout.lifeCounterTextP2TwoPlayer.text = p2.life.toString()
                setLifeTapListener(binding.twoPlayerLayout.lifeCounterTextP1TwoPlayer, 0)
                setLifeTapListener(binding.twoPlayerLayout.lifeCounterTextP2TwoPlayer, 1)
            }
            3 -> {
                val (p1, p2, p3) = gameState.players
                binding.threePlayerLayout.lifeCounterTextP1ThreePlayer.text = p1.life.toString()
                binding.threePlayerLayout.lifeCounterTextP2ThreePlayer.text = p2.life.toString()
                binding.threePlayerLayout.lifeCounterTextP3ThreePlayer.text = p3.life.toString()
                setLifeTapListener(binding.threePlayerLayout.lifeCounterTextP1ThreePlayer, 0)
                setLifeTapListener(binding.threePlayerLayout.lifeCounterTextP2ThreePlayer, 1)
                setLifeTapListener(binding.threePlayerLayout.lifeCounterTextP3ThreePlayer, 2)
            }
            4 -> {
                val (p1, p2, p3, p4) = gameState.players
                binding.fourPlayerLayout.lifeCounterTextP1FourPlayer.text = p1.life.toString()
                binding.fourPlayerLayout.lifeCounterTextP2FourPlayer.text = p2.life.toString()
                binding.fourPlayerLayout.lifeCounterTextP3FourPlayer.text = p3.life.toString()
                binding.fourPlayerLayout.lifeCounterTextP4FourPlayer.text = p4.life.toString()
                setLifeTapListener(binding.fourPlayerLayout.lifeCounterTextP1FourPlayer, 0)
                setLifeTapListener(binding.fourPlayerLayout.lifeCounterTextP2FourPlayer, 1)
                setLifeTapListener(binding.fourPlayerLayout.lifeCounterTextP3FourPlayer, 2)
                setLifeTapListener(binding.fourPlayerLayout.lifeCounterTextP4FourPlayer, 3)
            }
            5 -> {
                val (p1, p2, p3, p4, p5) = gameState.players
                binding.fivePlayerLayout.lifeCounterTextP1FivePlayer.text = p1.life.toString()
                binding.fivePlayerLayout.lifeCounterTextP2FivePlayer.text = p2.life.toString()
                binding.fivePlayerLayout.lifeCounterTextP3FivePlayer.text = p3.life.toString()
                binding.fivePlayerLayout.lifeCounterTextP4FivePlayer.text = p4.life.toString()
                binding.fivePlayerLayout.lifeCounterTextP5FivePlayer.text = p5.life.toString()
                setLifeTapListener(binding.fivePlayerLayout.lifeCounterTextP1FivePlayer, 0)
                setLifeTapListener(binding.fivePlayerLayout.lifeCounterTextP2FivePlayer, 1)
                setLifeTapListener(binding.fivePlayerLayout.lifeCounterTextP3FivePlayer, 2)
                setLifeTapListener(binding.fivePlayerLayout.lifeCounterTextP4FivePlayer, 3)
                setLifeTapListener(binding.fivePlayerLayout.lifeCounterTextP5FivePlayer, 4)
            }
            6 -> {
                val players = gameState.players
                binding.sixPlayerLayout.lifeCounterTextP1SixPlayer.text = players[0].life.toString()
                binding.sixPlayerLayout.lifeCounterTextP2SixPlayer.text = players[1].life.toString()
                binding.sixPlayerLayout.lifeCounterTextP3SixPlayer.text = players[2].life.toString()
                binding.sixPlayerLayout.lifeCounterTextP4SixPlayer.text = players[3].life.toString()
                binding.sixPlayerLayout.lifeCounterTextP5SixPlayer.text = players[4].life.toString()
                binding.sixPlayerLayout.lifeCounterTextP6SixPlayer.text = players[5].life.toString()
                setLifeTapListener(binding.sixPlayerLayout.lifeCounterTextP1SixPlayer, 0)
                setLifeTapListener(binding.sixPlayerLayout.lifeCounterTextP2SixPlayer, 1)
                setLifeTapListener(binding.sixPlayerLayout.lifeCounterTextP3SixPlayer, 2)
                setLifeTapListener(binding.sixPlayerLayout.lifeCounterTextP4SixPlayer, 3)
                setLifeTapListener(binding.sixPlayerLayout.lifeCounterTextP5SixPlayer, 4)
                setLifeTapListener(binding.sixPlayerLayout.lifeCounterTextP6SixPlayer, 5)
            }
        }
    }

    private fun setLifeTapListener(view: LifeCounterView, playerIndex: Int) {
        // This function remains the same as before
        view.onLifeIncreasedListener = {
            gameViewModel.increaseLife(playerIndex)
        }
        view.onLifeDecreasedListener = {
            gameViewModel.decreaseLife(playerIndex)
        }
    }

    private fun showSettingsPopup() {
        // REMOVED "Reset Game" from the options
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
                when (which) {
                    0 -> showPlayerCountSelection()
                    // Case for "Reset Game" is removed
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showPlayerCountSelection() {
        // This function remains the same as before
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