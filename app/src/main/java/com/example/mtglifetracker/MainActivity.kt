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
    private lateinit var fourPlayerLayoutContainer: LinearLayout // Added

    // TextViews for 2-Player Layout
    private lateinit var lifeCounterTextP1TwoPlayer: TextView
    private lateinit var lifeCounterTextP2TwoPlayer: TextView

    // TextViews for 3-Player Layout
    private lateinit var lifeCounterTextP1ThreePlayer: TextView
    private lateinit var lifeCounterTextP2ThreePlayer: TextView
    private lateinit var lifeCounterTextP3ThreePlayer: TextView

    // TextViews for 4-Player Layout (Added)
    private lateinit var lifeCounterTextP1FourPlayer: TextView
    private lateinit var lifeCounterTextP2FourPlayer: TextView
    private lateinit var lifeCounterTextP3FourPlayer: TextView
    private lateinit var lifeCounterTextP4FourPlayer: TextView


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
        fourPlayerLayoutContainer = findViewById(R.id.fourPlayerLayoutContainer) // Added

        // Initialize 2-Player TextViews
        lifeCounterTextP1TwoPlayer = findViewById(R.id.lifeCounterTextP1TwoPlayer)
        lifeCounterTextP2TwoPlayer = findViewById(R.id.lifeCounterTextP2TwoPlayer)

        // Initialize 3-Player TextViews
        lifeCounterTextP1ThreePlayer = findViewById(R.id.lifeCounterTextP1ThreePlayer)
        lifeCounterTextP2ThreePlayer = findViewById(R.id.lifeCounterTextP2ThreePlayer)
        lifeCounterTextP3ThreePlayer = findViewById(R.id.lifeCounterTextP3ThreePlayer)

        // Initialize 4-Player TextViews (Added)
        lifeCounterTextP1FourPlayer = findViewById(R.id.lifeCounterTextP1FourPlayer)
        lifeCounterTextP2FourPlayer = findViewById(R.id.lifeCounterTextP2FourPlayer)
        lifeCounterTextP3FourPlayer = findViewById(R.id.lifeCounterTextP3FourPlayer)
        lifeCounterTextP4FourPlayer = findViewById(R.id.lifeCounterTextP4FourPlayer)

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
        lifeCounterTextP1FourPlayer.setOnTouchListener(null) // Added
        lifeCounterTextP2FourPlayer.setOnTouchListener(null) // Added
        lifeCounterTextP3FourPlayer.setOnTouchListener(null) // Added
        lifeCounterTextP4FourPlayer.setOnTouchListener(null) // Added


        twoPlayerLayoutContainer.visibility = View.GONE
        threePlayerLayoutContainer.visibility = View.GONE
        fourPlayerLayoutContainer.visibility = View.GONE


        when (playerCount) {
            2 -> {
                twoPlayerLayoutContainer.visibility = View.VISIBLE
                updateLifeDisplay(0) // P1
                updateLifeDisplay(1) // P2
                lifeCounterTextP1TwoPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 0) }
                lifeCounterTextP2TwoPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 1) }
            }
            3 -> {
                threePlayerLayoutContainer.visibility = View.VISIBLE
                updateLifeDisplay(0) // P1
                updateLifeDisplay(1) // P2
                updateLifeDisplay(2) // P3
                lifeCounterTextP1ThreePlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 0) }
                lifeCounterTextP2ThreePlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 1) }
                lifeCounterTextP3ThreePlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 2) }
            }
            4 -> { // Added case for 4 players
                fourPlayerLayoutContainer.visibility = View.VISIBLE
                updateLifeDisplay(0) // P1
                updateLifeDisplay(1) // P2
                updateLifeDisplay(3) // P4
                lifeCounterTextP1FourPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 0) }
                lifeCounterTextP2FourPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 1) }
                lifeCounterTextP3FourPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 2) }
                lifeCounterTextP4FourPlayer.setOnTouchListener { view, event -> handleLifeTap(event, view, 3) }
            }
            else -> {
                Toast.makeText(this, "$newPlayerCount players UI not yet implemented. Reverting to 2 players.", Toast.LENGTH_LONG).show()
                setupUIForPlayerCount(2)
            }
        }
    }

    private fun handleLifeTap(event: MotionEvent, view: View, playerIndex: Int): Boolean {
        if (playerIndex >= players.size) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y // Y coordinate might be needed for some orientations
            val viewWidth = view.width
            val viewHeight = view.height

            // Determine tap area based on view rotation and player position
            // This logic assumes standard horizontal or vertical splits.
            // For 4 players, P1 & P2 are top, P3 & P4 are bottom.
            // P1 (top-left, rotated 180): left half decreases, right half increases
            // P2 (top-right, rotated 180): left half decreases, right half increases
            // P3 (bottom-left, rotation 0): left half decreases, right half increases
            // P4 (bottom-right, rotation 0): left half decreases, right half increases

            // For views rotated 90 or -90 (like P2 and P3 in 3-player mode):
            // Rotation 90: top half decreases, bottom half increases
            // Rotation -90: bottom half decreases, top half increases

            var decrease = false
            val rotation = view.rotation

            if (rotation == 180f || rotation == 0f) { // Horizontal split
                if (touchX < viewWidth / 2) {
                    decrease = true
                }
            } else if (rotation == 90f) { // Vertical split, text upright towards right
                if (touchY < viewHeight / 2) { // Top half
                    decrease = true
                }
            } else if (rotation == -90f) { // Vertical split, text upright towards left
                if (touchY > viewHeight / 2) { // Bottom half
                    decrease = true
                }
            }


            if (decrease) {
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
            4 -> { // Added case for 4 players
                when (playerIndex) {
                    0 -> lifeCounterTextP1FourPlayer.text = lifeTotal
                    1 -> lifeCounterTextP2FourPlayer.text = lifeTotal
                    2 -> lifeCounterTextP3FourPlayer.text = lifeTotal
                    3 -> lifeCounterTextP4FourPlayer.text = lifeTotal
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

                if (selectedPlayerCount == 2 || selectedPlayerCount == 3 || selectedPlayerCount == 4) { // Updated condition
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