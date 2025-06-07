package com.example.mtglifetracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mtglifetracker.model.Player
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Handles the direct read/write operations with SharedPreferences.
 * This class abstracts the data storage mechanism.
 */
class GamePreferences(context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mtg_game_state", Context.MODE_PRIVATE)

    private val KEY_PLAYER_COUNT = "player_count"
    private val KEY_PLAYERS_LIST = "players_list"

    /**
     * Saves the number of players.
     */
    fun savePlayerCount(count: Int) {
        prefs.edit().putInt(KEY_PLAYER_COUNT, count).apply()
    }

    /**
     * Retrieves the saved number of players, defaulting to 2.
     */
    fun getPlayerCount(): Int {
        return prefs.getInt(KEY_PLAYER_COUNT, 2)
    }

    /**
     * Saves the entire list of players by converting it to a JSON string.
     */
    fun savePlayers(players: List<Player>) {
        val json = gson.toJson(players)
        prefs.edit().putString(KEY_PLAYERS_LIST, json).apply()
    }

    /**
     * Retrieves the list of players by reading the JSON string and converting it back.
     * Returns a default list of 2 players if no data is saved.
     */
    fun getPlayers(): List<Player> {
        val json = prefs.getString(KEY_PLAYERS_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<Player>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Default to a 2-player game if nothing is saved
            listOf(Player(name = "Player 1"), Player(name = "Player 2"))
        }
    }
}