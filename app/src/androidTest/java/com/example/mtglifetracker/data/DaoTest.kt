package com.example.mtglifetracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumentation tests for the Room database DAOs (PlayerDao and GameSettingsDao).
 * This class uses an in-memory database to ensure tests are fast and hermetic.
 */
@RunWith(AndroidJUnit4::class)
class DaosTest {

    private lateinit var db: AppDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var settingsDao: GameSettingsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        playerDao = db.playerDao()
        settingsDao = db.gameSettingsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun settingsDao_saveAndGetSettings() = runTest {
        val newSettings = GameSettings(playerCount = 4, startingLife = 20)
        settingsDao.saveSettings(newSettings)

        val retrievedSettings = settingsDao.getSettings().first()
        assertEquals(4, retrievedSettings?.playerCount)
        assertEquals(20, retrievedSettings?.startingLife)
    }

    @Test
    @Throws(Exception::class)
    fun playerDao_insertAndGetPlayers() = runTest {
        // ***FIXED***: Added the 'life' parameter here
        val players = listOf(
            Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40),
            Player(gameSize = 2, playerIndex = 1, name = "Player 2", life = 40)
        )
        playerDao.insertAll(players)

        val retrievedPlayers = playerDao.getPlayers(2).first()
        assertEquals(2, retrievedPlayers.size)
        assertEquals("Player 1", retrievedPlayers[0].name)
    }

    @Test
    @Throws(Exception::class)
    fun playerDao_updatePlayer() = runTest {
        // ***FIXED***: Added the 'life' parameter here
        val player = Player(gameSize = 2, playerIndex = 0, life = 40)
        playerDao.insertAll(listOf(player))

        val updatedPlayer = player.copy(life = 35)
        playerDao.updatePlayer(updatedPlayer)

        val retrievedPlayers = playerDao.getPlayers(2).first()
        assertEquals(35, retrievedPlayers[0].life)
    }

    @Test
    @Throws(Exception::class)
    fun playerDao_deletePlayersForGame() = runTest {
        // ***FIXED***: Added the 'life' parameter here
        val players = listOf(
            Player(gameSize = 2, playerIndex = 0, life = 40),
            Player(gameSize = 4, playerIndex = 0, life = 20)
        )
        playerDao.insertAll(players)

        playerDao.deletePlayersForGame(gameSize = 2)

        val playersForGame2 = playerDao.getPlayers(2).first()
        val playersForGame4 = playerDao.getPlayers(4).first()

        assertTrue(playersForGame2.isEmpty())
        assertEquals(1, playersForGame4.size)
    }

    @Test
    @Throws(Exception::class)
    fun playerDao_deleteAll() = runTest {
        // ***FIXED***: Added the 'life' parameter here
        val players = listOf(
            Player(gameSize = 2, playerIndex = 0, life = 40),
            Player(gameSize = 4, playerIndex = 0, life = 20)
        )
        playerDao.insertAll(players)

        playerDao.deleteAll()

        val retrievedPlayers = playerDao.getPlayers(2).first()
        assertTrue(retrievedPlayers.isEmpty())
    }
}