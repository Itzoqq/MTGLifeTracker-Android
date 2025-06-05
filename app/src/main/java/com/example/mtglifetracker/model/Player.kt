package com.example.mtglifetracker.model

data class Player(
    var life: Int = 40,
    var name: String = "Player"
) {
    fun increaseLife(amount: Int = 1) {
        life += amount
    }

    fun decreaseLife(amount: Int = 1) {
        life -= amount
    }

    fun resetLife(startingLife: Int = 40) {
        life = startingLife
    }
}