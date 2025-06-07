package com.example.mtglifetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.model.Player

data class GameState(
    val playerCount: Int = 2,
    val players: List<Player> = emptyList()
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    val gameState = repository.gameState

    fun changePlayerCount(newPlayerCount: Int) {
        repository.changePlayerCount(newPlayerCount)
    }

    fun increaseLife(playerIndex: Int) {
        repository.increaseLife(playerIndex)
    }

    fun decreaseLife(playerIndex: Int) {
        repository.decreaseLife(playerIndex)
    }
}