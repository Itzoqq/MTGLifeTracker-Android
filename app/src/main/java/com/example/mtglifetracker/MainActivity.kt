package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.view.LifeCounterView

class MainActivity : AppCompatActivity() {

    // Layout Containers
    private lateinit var twoPlayerLayoutContainer: ConstraintLayout
    private lateinit var threePlayerLayoutContainer: ConstraintLayout
    private lateinit var fourPlayerLayoutContainer: ConstraintLayout
    private lateinit var fivePlayerLayoutContainer: ConstraintLayout
    private lateinit var sixPlayerLayoutContainer: ConstraintLayout

    // Views are now of the custom type LifeCounterView
    private lateinit var lifeCounterTextP1TwoPlayer: LifeCounterView
    private lateinit var lifeCounterTextP2TwoPlayer: LifeCounterView
    private lateinit var lifeCounterTextP1ThreePlayer: LifeCounterView
    private lateinit var lifeCounterTextP2ThreePlayer: LifeCounterView
    private lateinit var lifeCounterTextP3ThreePlayer: LifeCounterView
    private lateinit var lifeCounterTextP1FourPlayer: LifeCounterView
    private lateinit var lifeCounterTextP2FourPlayer: LifeCounterView
    private lateinit var lifeCounterTextP3FourPlayer: LifeCounterView
    private lateinit var lifeCounterTextP4FourPlayer: LifeCounterView
    private lateinit var lifeCounterTextP1FivePlayer: LifeCounterView
    private lateinit var lifeCounterTextP2FivePlayer: LifeCounterView
    private lateinit var lifeCounterTextP3FivePlayer: LifeCounterView
    private lateinit var lifeCounterTextP4FivePlayer: LifeCounterView
    private lateinit var lifeCounterTextP5FivePlayer: LifeCounterView
    private lateinit var lifeCounterTextP1SixPlayer: LifeCounterView
    private lateinit var lifeCounterTextP2SixPlayer: LifeCounterView
    private lateinit var lifeCounterTextP3SixPlayer: LifeCounterView
    private lateinit var lifeCounterTextP4SixPlayer: LifeCounterView
    private lateinit var lifeCounterTextP5SixPlayer: LifeCounterView
    private lateinit var lifeCounterTextP6SixPlayer: LifeCounterView

    // Common UI
    private lateinit var settingsIcon: ImageView

    private val players = mutableListOf<Player>()
    private var playerCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Find all views
        bindViews()

        settingsIcon.setOnClickListener {
            showSettingsPopup()
        }

        setupUIForPlayerCount(playerCount)
    }

    private fun setupUIForPlayerCount(newPlayerCount: Int) {
        playerCount = newPlayerCount

        players.clear()
        for (i in 0 until playerCount) {
            players.add(Player(name = "Player ${i + 1}"))
        }

        // Detach all listeners before setting new ones
        clearAllListeners()

        // Hide all layout containers
        twoPlayerLayoutContainer.visibility = View.GONE
        threePlayerLayoutContainer.visibility = View.GONE
        fourPlayerLayoutContainer.visibility = View.GONE
        fivePlayerLayoutContainer.visibility = View.GONE
        sixPlayerLayoutContainer.visibility = View.GONE

        // Configure the appropriate layout based on player count
        when (playerCount) {
            2 -> {
                twoPlayerLayoutContainer.visibility = View.VISIBLE
                (0..1).forEach { updateLifeDisplay(it) }
                // Set listeners for the custom view
                setLifeTapListener(lifeCounterTextP1TwoPlayer, 0)
                setLifeTapListener(lifeCounterTextP2TwoPlayer, 1)
            }
            3 -> {
                threePlayerLayoutContainer.visibility = View.VISIBLE
                (0..2).forEach { updateLifeDisplay(it) }
                setLifeTapListener(lifeCounterTextP1ThreePlayer, 0)
                setLifeTapListener(lifeCounterTextP2ThreePlayer, 1)
                setLifeTapListener(lifeCounterTextP3ThreePlayer, 2)
            }
            4 -> {
                fourPlayerLayoutContainer.visibility = View.VISIBLE
                (0..3).forEach { updateLifeDisplay(it) }
                setLifeTapListener(lifeCounterTextP1FourPlayer, 0)
                setLifeTapListener(lifeCounterTextP2FourPlayer, 1)
                setLifeTapListener(lifeCounterTextP3FourPlayer, 2)
                setLifeTapListener(lifeCounterTextP4FourPlayer, 3)
            }
            5 -> {
                fivePlayerLayoutContainer.visibility = View.VISIBLE
                (0..4).forEach { updateLifeDisplay(it) }
                setLifeTapListener(lifeCounterTextP1FivePlayer, 0)
                setLifeTapListener(lifeCounterTextP2FivePlayer, 1)
                setLifeTapListener(lifeCounterTextP3FivePlayer, 2)
                setLifeTapListener(lifeCounterTextP4FivePlayer, 3)
                setLifeTapListener(lifeCounterTextP5FivePlayer, 4)
            }
            6 -> {
                sixPlayerLayoutContainer.visibility = View.VISIBLE
                (0..5).forEach { updateLifeDisplay(it) }
                setLifeTapListener(lifeCounterTextP1SixPlayer, 0)
                setLifeTapListener(lifeCounterTextP2SixPlayer, 1)
                setLifeTapListener(lifeCounterTextP3SixPlayer, 2)
                setLifeTapListener(lifeCounterTextP4SixPlayer, 3)
                setLifeTapListener(lifeCounterTextP5SixPlayer, 4)
                setLifeTapListener(lifeCounterTextP6SixPlayer, 5)
            }
            else -> {
                Toast.makeText(this, "$newPlayerCount players UI not yet implemented. Reverting to 2 players.", Toast.LENGTH_LONG).show()
                setupUIForPlayerCount(2)
            }
        }
    }

    // Helper function to set listeners on our custom LifeCounterView
    private fun setLifeTapListener(view: LifeCounterView, playerIndex: Int) {
        view.onLifeIncreasedListener = {
            players[playerIndex].increaseLife()
            updateLifeDisplay(playerIndex)
        }
        view.onLifeDecreasedListener = {
            players[playerIndex].decreaseLife()
            updateLifeDisplay(playerIndex)
        }
    }

    private fun updateLifeDisplay(playerIndex: Int) {
        if (playerIndex >= players.size) return

        val lifeTotal = players[playerIndex].life.toString()

        when (playerCount) {
            2 -> {
                if (playerIndex == 0) lifeCounterTextP1TwoPlayer.text = lifeTotal
                else lifeCounterTextP2TwoPlayer.text = lifeTotal
            }
            3 -> {
                when (playerIndex) {
                    0 -> lifeCounterTextP1ThreePlayer.text = lifeTotal
                    1 -> lifeCounterTextP2ThreePlayer.text = lifeTotal
                    2 -> lifeCounterTextP3ThreePlayer.text = lifeTotal
                }
            }
            4 -> {
                when (playerIndex) {
                    0 -> lifeCounterTextP1FourPlayer.text = lifeTotal
                    1 -> lifeCounterTextP2FourPlayer.text = lifeTotal
                    2 -> lifeCounterTextP3FourPlayer.text = lifeTotal
                    3 -> lifeCounterTextP4FourPlayer.text = lifeTotal
                }
            }
            5 -> {
                when (playerIndex) {
                    0 -> lifeCounterTextP1FivePlayer.text = lifeTotal
                    1 -> lifeCounterTextP2FivePlayer.text = lifeTotal
                    2 -> lifeCounterTextP3FivePlayer.text = lifeTotal
                    3 -> lifeCounterTextP4FivePlayer.text = lifeTotal
                    4 -> lifeCounterTextP5FivePlayer.text = lifeTotal
                }
            }
            6 -> {
                when (playerIndex) {
                    0 -> lifeCounterTextP1SixPlayer.text = lifeTotal
                    1 -> lifeCounterTextP2SixPlayer.text = lifeTotal
                    2 -> lifeCounterTextP3SixPlayer.text = lifeTotal
                    3 -> lifeCounterTextP4SixPlayer.text = lifeTotal
                    4 -> lifeCounterTextP5SixPlayer.text = lifeTotal
                    5 -> lifeCounterTextP6SixPlayer.text = lifeTotal
                }
            }
        }
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
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: this.playerCount
                if (selectedPlayerCount in 2..6) {
                    setupUIForPlayerCount(selectedPlayerCount)
                } else {
                    Toast.makeText(this, "$selectedPlayerCount players UI not yet implemented. Setting to 2 players.", Toast.LENGTH_LONG).show()
                    setupUIForPlayerCount(2)
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Helper function to keep onCreate clean
    private fun bindViews() {
        twoPlayerLayoutContainer = findViewById(R.id.twoPlayerLayoutContainer)
        threePlayerLayoutContainer = findViewById(R.id.threePlayerLayoutContainer)
        fourPlayerLayoutContainer = findViewById(R.id.fourPlayerLayoutContainer)
        fivePlayerLayoutContainer = findViewById(R.id.fivePlayerLayoutContainer)
        sixPlayerLayoutContainer = findViewById(R.id.sixPlayerLayoutContainer)
        lifeCounterTextP1TwoPlayer = findViewById(R.id.lifeCounterTextP1TwoPlayer)
        lifeCounterTextP2TwoPlayer = findViewById(R.id.lifeCounterTextP2TwoPlayer)
        lifeCounterTextP1ThreePlayer = findViewById(R.id.lifeCounterTextP1ThreePlayer)
        lifeCounterTextP2ThreePlayer = findViewById(R.id.lifeCounterTextP2ThreePlayer)
        lifeCounterTextP3ThreePlayer = findViewById(R.id.lifeCounterTextP3ThreePlayer)
        lifeCounterTextP1FourPlayer = findViewById(R.id.lifeCounterTextP1FourPlayer)
        lifeCounterTextP2FourPlayer = findViewById(R.id.lifeCounterTextP2FourPlayer)
        lifeCounterTextP3FourPlayer = findViewById(R.id.lifeCounterTextP3FourPlayer)
        lifeCounterTextP4FourPlayer = findViewById(R.id.lifeCounterTextP4FourPlayer)
        lifeCounterTextP1FivePlayer = findViewById(R.id.lifeCounterTextP1FivePlayer)
        lifeCounterTextP2FivePlayer = findViewById(R.id.lifeCounterTextP2FivePlayer)
        lifeCounterTextP3FivePlayer = findViewById(R.id.lifeCounterTextP3FivePlayer)
        lifeCounterTextP4FivePlayer = findViewById(R.id.lifeCounterTextP4FivePlayer)
        lifeCounterTextP5FivePlayer = findViewById(R.id.lifeCounterTextP5FivePlayer)
        lifeCounterTextP1SixPlayer = findViewById(R.id.lifeCounterTextP1SixPlayer)
        lifeCounterTextP2SixPlayer = findViewById(R.id.lifeCounterTextP2SixPlayer)
        lifeCounterTextP3SixPlayer = findViewById(R.id.lifeCounterTextP3SixPlayer)
        lifeCounterTextP4SixPlayer = findViewById(R.id.lifeCounterTextP4SixPlayer)
        lifeCounterTextP5SixPlayer = findViewById(R.id.lifeCounterTextP5SixPlayer)
        lifeCounterTextP6SixPlayer = findViewById(R.id.lifeCounterTextP6SixPlayer)
        settingsIcon = findViewById(R.id.settingsIcon)
    }

    // Helper function to clear all listeners
    private fun clearAllListeners() {
        val allLifeCounterViews = listOf(
            lifeCounterTextP1TwoPlayer, lifeCounterTextP2TwoPlayer,
            lifeCounterTextP1ThreePlayer, lifeCounterTextP2ThreePlayer, lifeCounterTextP3ThreePlayer,
            lifeCounterTextP1FourPlayer, lifeCounterTextP2FourPlayer, lifeCounterTextP3FourPlayer, lifeCounterTextP4FourPlayer,
            lifeCounterTextP1FivePlayer, lifeCounterTextP2FivePlayer, lifeCounterTextP3FivePlayer, lifeCounterTextP4FivePlayer, lifeCounterTextP5FivePlayer,
            lifeCounterTextP1SixPlayer, lifeCounterTextP2SixPlayer, lifeCounterTextP3SixPlayer, lifeCounterTextP4SixPlayer, lifeCounterTextP5SixPlayer, lifeCounterTextP6SixPlayer
        )
        allLifeCounterViews.forEach {
            it.onLifeIncreasedListener = null
            it.onLifeDecreasedListener = null
        }
    }
}