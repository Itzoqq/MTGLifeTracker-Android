package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout // Ensure LinearLayout is imported
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mtglifetracker.model.Player

class MainActivity : AppCompatActivity() {

    // Layout Containers
    private lateinit var twoPlayerLayoutContainer: LinearLayout
    private lateinit var threePlayerLayoutContainer: LinearLayout

    // TextViews for 2-Player Layout
    private lateinit var lifeCounterTextP1TwoPlayer: TextView
    private lateinit var lifeCounterTextP2TwoPlayer: TextView

    // TextViews for 3-Player Layout
    private lateinit var lifeCounterTextP1ThreePlayer: TextView
    private lateinit var lifeCounterTextP2ThreePlayer: TextView
    private lateinit var lifeCounterTextP3ThreePlayer: TextView

    // Common UI
    private lateinit var settingsIcon: ImageView

    private val players = mutableListOf<Player>()
    private var playerCount = 2 // Default to 2 players

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Initialize layout containers
        twoPlayerLayoutContainer = findViewById(R.id.twoPlayerLayoutContainer)
        threePlayerLayoutContainer = findViewById(R.id.threePlayerLayoutContainer)

        // Initialize 2-Player TextViews
        lifeCounterTextP1TwoPlayer = findViewById(R.id.lifeCounterTextP1TwoPlayer)
        lifeCounterTextP2TwoPlayer = findViewById(R.id.lifeCounterTextP2TwoPlayer)

        // Initialize 3-Player TextViews
        lifeCounterTextP1ThreePlayer = findViewById(R.id.lifeCounterTextP1ThreePlayer)
        lifeCounterTextP2ThreePlayer = findViewById(R.id.lifeCounterTextP2ThreePlayer)
        lifeCounterTextP3ThreePlayer = findViewById(R.id.lifeCounterTextP3ThreePlayer)

        settingsIcon = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            showSettingsPopup()
        }

        setupUIForPlayerCount(playerCount) // Initialize UI for default 2 players
    }

    private fun setupUIForPlayerCount(newPlayerCount: Int) {
        playerCount = newPlayerCount // Update the class member

        players.clear()
        for (i in 0 until playerCount) {
            players.add(Player(name = "Player ${i + 1}"))
        }

        // Detach all listeners first to prevent issues
        lifeCounterTextP1TwoPlayer.setOnTouchListener(null)
        lifeCounterTextP2TwoPlayer.setOnTouchListener(null)
        lifeCounterTextP1ThreePlayer.setOnTouchListener(null)
        lifeCounterTextP2ThreePlayer.setOnTouchListener(null)
        lifeCounterTextP3ThreePlayer.setOnTouchListener(null)

        when (playerCount) {
            2 -> {
                twoPlayerLayoutContainer.visibility = View.VISIBLE
                threePlayerLayoutContainer.visibility = View.GONE

                updateLifeDisplay(0) // P1
                updateLifeDisplay(1) // P2

                lifeCounterTextP1TwoPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 0) }
                lifeCounterTextP2TwoPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 1) }
            }
            3 -> {
                twoPlayerLayoutContainer.visibility = View.GONE
                threePlayerLayoutContainer.visibility = View.VISIBLE

                updateLifeDisplay(0) // P1
                updateLifeDisplay(1) // P2
                updateLifeDisplay(2) // P3

                lifeCounterTextP1ThreePlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 0) }
                lifeCounterTextP2ThreePlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 1) }
                lifeCounterTextP3ThreePlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 2) }
            }
            else -> {
                // This case handles playerCount values other than 2 or 3 (e.g., 4, 5, 6 from selection)
                // Default to 2 players and show a message if UI not implemented.
                // The redundant 'if (this.playerCount != 2)' condition is removed from here.
                Toast.makeText(this, "$newPlayerCount players UI not yet implemented. Reverting to 2 players.", Toast.LENGTH_LONG).show()
                setupUIForPlayerCount(2) // Recursively call to set up for 2 players
            }
        }
    }

    private fun handleLifeTap(event: MotionEvent, view: View, playerIndex: Int): Boolean {
        if (playerIndex >= players.size) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val textWidth = view.width
            val touchX = event.x

            if (touchX < textWidth / 2) {
                players[playerIndex].decreaseLife()
            } else {
                players[playerIndex].increaseLife()
            }
            updateLifeDisplay(playerIndex)
            view.performClick() // Call performClick for accessibility
            return true
        }
        return false
    }

    private fun updateLifeDisplay(playerIndex: Int) {
        if (playerIndex >= players.size) return

        val lifeTotal = players[playerIndex].life.toString()

        when (playerCount) {
            2 -> {
                if (playerIndex == 0) lifeCounterTextP1TwoPlayer.text = lifeTotal
                else if (playerIndex == 1) lifeCounterTextP2TwoPlayer.text = lifeTotal
            }
            3 -> {
                when (playerIndex) {
                    0 -> lifeCounterTextP1ThreePlayer.text = lifeTotal
                    1 -> lifeCounterTextP2ThreePlayer.text = lifeTotal
                    2 -> lifeCounterTextP3ThreePlayer.text = lifeTotal
                }
            }
        }
    }

    private fun showSettingsPopup() {
        val settingsOptions = arrayOf("Number of Players")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingsOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.WHITE)
                return view
            }
        }

        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Settings")
            .setAdapter(adapter) { dialog, which ->
                when (which) {
                    0 -> showPlayerCountSelection()
                }
                dialog.dismiss()
            }
            .setOnCancelListener { /* Dialog automatically closes */ }
            .create()
            .show()
    }

    private fun showPlayerCountSelection() {
        val playerCountOptions = arrayOf("2", "3", "4", "5", "6") // Options available

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playerCountOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(Color.WHITE)
                return view
            }
        }

        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Number of Players")
            .setAdapter(adapter) { dialog, which ->
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: this.playerCount

                if (selectedPlayerCount == 2 || selectedPlayerCount == 3) {
                    setupUIForPlayerCount(selectedPlayerCount)
                } else {
                    Toast.makeText(this, "$selectedPlayerCount players UI not yet implemented. Setting to 2 players.", Toast.LENGTH_LONG).show()
                    setupUIForPlayerCount(2) // Default to 2 if UI for more isn't ready
                }
                dialog.dismiss()
            }
            .setOnCancelListener { /* Dialog automatically closes */ }
            .create()
            .show()
    }
}