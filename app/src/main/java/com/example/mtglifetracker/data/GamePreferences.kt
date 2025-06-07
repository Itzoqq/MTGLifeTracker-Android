package com.example.mtglifetracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mtglifetracker.model.Player
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

/**
 * Handles the direct read and write operations with SharedPreferences.
 * This class acts as the data source, abstracting the storage mechanism from the rest of the app.
 *
 * @param context The application context, used to get SharedPreferences.
 */
class GamePreferences(context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Saves the current number of players.
     */
    fun savePlayerCount(count: Int) {
        prefs.edit { putInt(KEY_PLAYER_COUNT, count) }
    }

    /**
     * Retrieves the saved number of players, defaulting to 2.
     */
    fun getPlayerCount(): Int {
        return prefs.getInt(KEY_PLAYER_COUNT, 2)
    }

    /**
     * Saves the list of players by converting it to a JSON string using Gson.
     */
    fun savePlayers(players: List<Player>) {
        val json = gson.toJson(players)
        prefs.edit { putString(KEY_PLAYERS_LIST, json) }
    }

    /**
     * Retrieves the list of players by reading the JSON string and converting it back.
     * Returns a default 2-player list if no data is found.
     */
    fun getPlayers(): List<Player> {
        val json = prefs.getString(KEY_PLAYERS_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<Player>>() {}.type
            gson.fromJson(json, type)
        } else {
            listOf(Player(name = "Player 1"), Player(name = "Player 2"))
        }
    }

    /**
     * Companion object to hold constant values.
     */
    companion object {
        private const val PREFS_NAME = "mtg_game_state"
        private const val KEY_PLAYER_COUNT = "player_count"
        private const val KEY_PLAYERS_LIST = "players_list"
    }
}