package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mtglifetracker.model.Player

class MainActivity : AppCompatActivity() {

    // Layout Containers
    private lateinit var twoPlayerLayoutContainer: LinearLayout
    private lateinit var threePlayerLayoutContainer: LinearLayout
    private lateinit var fourPlayerLayoutContainer: LinearLayout
    private lateinit var fivePlayerLayoutContainer: LinearLayout // NEW

    // TextViews for 2-Player Layout
    private lateinit var lifeCounterTextP1TwoPlayer: TextView
    private lateinit var lifeCounterTextP2TwoPlayer: TextView

    // TextViews for 3-Player Layout
    private lateinit var lifeCounterTextP1ThreePlayer: TextView
    private lateinit var lifeCounterTextP2ThreePlayer: TextView
    private lateinit var lifeCounterTextP3ThreePlayer: TextView

    // TextViews for 4-Player Layout
    private lateinit var lifeCounterTextP1FourPlayer: TextView
    private lateinit var lifeCounterTextP2FourPlayer: TextView
    private lateinit var lifeCounterTextP3FourPlayer: TextView
    private lateinit var lifeCounterTextP4FourPlayer: TextView

    // TextViews for 5-Player Layout // NEW
    private lateinit var lifeCounterTextP1FivePlayer: TextView
    private lateinit var lifeCounterTextP2FivePlayer: TextView
    private lateinit var lifeCounterTextP3FivePlayer: TextView
    private lateinit var lifeCounterTextP4FivePlayer: TextView
    private lateinit var lifeCounterTextP5FivePlayer: TextView

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
        fourPlayerLayoutContainer = findViewById(R.id.fourPlayerLayoutContainer)
        fivePlayerLayoutContainer = findViewById(R.id.fivePlayerLayoutContainer) // NEW

        // Initialize 2-Player TextViews
        lifeCounterTextP1TwoPlayer = findViewById(R.id.lifeCounterTextP1TwoPlayer)
        lifeCounterTextP2TwoPlayer = findViewById(R.id.lifeCounterTextP2TwoPlayer)

        // Initialize 3-Player TextViews
        lifeCounterTextP1ThreePlayer = findViewById(R.id.lifeCounterTextP1ThreePlayer)
        lifeCounterTextP2ThreePlayer = findViewById(R.id.lifeCounterTextP2ThreePlayer)
        lifeCounterTextP3ThreePlayer = findViewById(R.id.lifeCounterTextP3ThreePlayer)

        // Initialize 4-Player TextViews
        lifeCounterTextP1FourPlayer = findViewById(R.id.lifeCounterTextP1FourPlayer)
        lifeCounterTextP2FourPlayer = findViewById(R.id.lifeCounterTextP2FourPlayer)
        lifeCounterTextP3FourPlayer = findViewById(R.id.lifeCounterTextP3FourPlayer)
        lifeCounterTextP4FourPlayer = findViewById(R.id.lifeCounterTextP4FourPlayer)

        // Initialize 5-Player TextViews // NEW
        lifeCounterTextP1FivePlayer = findViewById(R.id.lifeCounterTextP1FivePlayer)
        lifeCounterTextP2FivePlayer = findViewById(R.id.lifeCounterTextP2FivePlayer)
        lifeCounterTextP3FivePlayer = findViewById(R.id.lifeCounterTextP3FivePlayer)
        lifeCounterTextP4FivePlayer = findViewById(R.id.lifeCounterTextP4FivePlayer)
        lifeCounterTextP5FivePlayer = findViewById(R.id.lifeCounterTextP5FivePlayer)


        settingsIcon = findViewById(R.id.settingsIcon)
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

        // Detach all listeners
        lifeCounterTextP1TwoPlayer.setOnTouchListener(null)
        lifeCounterTextP2TwoPlayer.setOnTouchListener(null)
        lifeCounterTextP1ThreePlayer.setOnTouchListener(null)
        lifeCounterTextP2ThreePlayer.setOnTouchListener(null)
        lifeCounterTextP3ThreePlayer.setOnTouchListener(null)
        lifeCounterTextP1FourPlayer.setOnTouchListener(null)
        lifeCounterTextP2FourPlayer.setOnTouchListener(null)
        lifeCounterTextP3FourPlayer.setOnTouchListener(null)
        lifeCounterTextP4FourPlayer.setOnTouchListener(null)
        lifeCounterTextP1FivePlayer.setOnTouchListener(null) // NEW
        lifeCounterTextP2FivePlayer.setOnTouchListener(null) // NEW
        lifeCounterTextP3FivePlayer.setOnTouchListener(null) // NEW
        lifeCounterTextP4FivePlayer.setOnTouchListener(null) // NEW
        lifeCounterTextP5FivePlayer.setOnTouchListener(null) // NEW

        // Hide all containers initially
        twoPlayerLayoutContainer.visibility = View.GONE
        threePlayerLayoutContainer.visibility = View.GONE
        fourPlayerLayoutContainer.visibility = View.GONE
        fivePlayerLayoutContainer.visibility = View.GONE // NEW

        when (playerCount) {
            2 -> {
                twoPlayerLayoutContainer.visibility = View.VISIBLE
                (0..1).forEach { updateLifeDisplay(it) }
                lifeCounterTextP1TwoPlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 0) }
                lifeCounterTextP2TwoPlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 1) }
            }
            3 -> {
                threePlayerLayoutContainer.visibility = View.VISIBLE
                (0..2).forEach { updateLifeDisplay(it) }
                lifeCounterTextP1ThreePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 0) }
                lifeCounterTextP2ThreePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 1) }
                lifeCounterTextP3ThreePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 2) }
            }
            4 -> {
                fourPlayerLayoutContainer.visibility = View.VISIBLE
                (0..3).forEach { updateLifeDisplay(it) }
                lifeCounterTextP1FourPlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 0) }
                lifeCounterTextP2FourPlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 1) }
                lifeCounterTextP3FourPlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 2) }
                lifeCounterTextP4FourPlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 3) }
            }
            5 -> { // NEW
                fivePlayerLayoutContainer.visibility = View.VISIBLE
                (0..4).forEach { updateLifeDisplay(it) }
                lifeCounterTextP1FivePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 0) }
                lifeCounterTextP2FivePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 1) }
                lifeCounterTextP3FivePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 2) }
                lifeCounterTextP4FivePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 3) }
                lifeCounterTextP5FivePlayer.setOnTouchListener { v, e -> handleLifeTap(e, v, 4) }
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
            val touchY = event.y
            val viewWidth = view.width
            val viewHeight = view.height

            var decrease = false
            when (view.rotation) {
                0f, 180f -> if (touchX < viewWidth / 2) decrease = true
                90f -> if (touchY < viewHeight / 2) decrease = true
                -90f, 270f -> if (touchY > viewHeight / 2) decrease = true
            }

            if (decrease) {
                players[playerIndex].decreaseLife()
            } else {
                players[playerIndex].increaseLife()
            }
            updateLifeDisplay(playerIndex)
            view.performClick()
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
            5 -> { // NEW
                when (playerIndex) {
                    0 -> lifeCounterTextP1FivePlayer.text = lifeTotal
                    1 -> lifeCounterTextP2FivePlayer.text = lifeTotal
                    2 -> lifeCounterTextP3FivePlayer.text = lifeTotal
                    3 -> lifeCounterTextP4FivePlayer.text = lifeTotal
                    4 -> lifeCounterTextP5FivePlayer.text = lifeTotal
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

                if (selectedPlayerCount in 2..5) { // UPDATED condition
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
}