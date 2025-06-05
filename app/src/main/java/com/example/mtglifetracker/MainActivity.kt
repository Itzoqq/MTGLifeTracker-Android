package com.example.mtglifetracker

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
// import android.widget.LinearLayout // No longer needed for singlePlayerLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mtglifetracker.model.Player

class MainActivity : AppCompatActivity() {
    // Single Player UI elements are removed
    // private lateinit var singlePlayerLayout: LinearLayout // REMOVED
    // private lateinit var lifeCounterText: TextView // REMOVED

    // Two Player UI
    // private lateinit var twoPlayerLayout: LinearLayout // Not strictly needed as a variable if always visible
    private lateinit var lifeCounterTextP1: TextView
    private lateinit var lifeCounterTextP2: TextView

    private lateinit var settingsIcon: ImageView

    private val players = mutableListOf<Player>()
    private var playerCount = 2 // Default to 2 players

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // FindViewById for single player UI elements are removed

        // Two Player UI
        // twoPlayerLayout = findViewById(R.id.twoPlayerLayout) // Not strictly needed if always visible
        lifeCounterTextP1 = findViewById(R.id.lifeCounterTextP1)
        lifeCounterTextP2 = findViewById(R.id.lifeCounterTextP2)

        settingsIcon = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            showSettingsPopup()
        }

        setupUIForPlayerCount(playerCount) // Initialize UI for default 2 players
    }

    private fun setupUIForPlayerCount(count: Int) {
        // Ensure count is at least 2, though the selection dialog will also enforce this.
        // For now, this function is primarily designed for count = 2.
        playerCount = if (count < 2) 2 else count

        players.clear()
        for (i in 0 until playerCount) { // Use the validated playerCount
            players.add(Player(name = "Player ${i + 1}"))
        }

        // Detach P1/P2 listeners first to avoid conflicts if re-setup
        lifeCounterTextP1.setOnTouchListener(null)
        lifeCounterTextP2.setOnTouchListener(null)

        // Logic for count == 1 is removed.
        // The twoPlayerLayout is now visible by default in XML.
        if (playerCount == 2) {
            updateLifeDisplay(0) // Update P1
            updateLifeDisplay(1) // Update P2

            lifeCounterTextP1.setOnTouchListener { view, event ->
                handleLifeTap(event, view, 0)
            }
            lifeCounterTextP2.setOnTouchListener { view, event ->
                handleLifeTap(event, view, 1)
            }
        }
        // TODO: Extend for more players (3, 4, etc.).
        // This would involve dynamically creating views or having more predefined layouts.
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
            return true
        }
        return false
    }

    private fun updateLifeDisplay(playerIndex: Int) {
        if (playerIndex >= players.size) return

        val lifeTotal = players[playerIndex].life.toString()

        // Since playerCount is now always >= 2, the 'when' block can be simplified
        // For now, this is specific to the 2-player UI.
        if (playerCount == 2) { //
            if (playerIndex == 0) lifeCounterTextP1.text = lifeTotal
            else if (playerIndex == 1) lifeCounterTextP2.text = lifeTotal
        }
        // TODO: Add cases for more players if UI supports them
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
        // Player count options now start from "2"
        val playerCountOptions = arrayOf("2", "3", "4", "5", "6")

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
                val selectedPlayerCount = playerCountOptions[which].toIntOrNull() ?: playerCount

                // Current UI only fully supports 2 players.
                // If a different number (e.g., 3, 4) is selected,
                // we'll show a message and default to 2 players for now.
                if (selectedPlayerCount == 2) {
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