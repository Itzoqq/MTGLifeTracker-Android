package com.example.mtglifetracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.model.GameSettings
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.util.Logger
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
 * Instrumented tests for the Room database DAOs ([PlayerDao] and [GameSettingsDao]).
 *
 * This class uses an in-memory database to ensure that tests are fast, isolated,
 * and do not affect the actual device database. Each test verifies a specific
 * database operation (insert, update, delete, query).
 */
@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: AppDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var settingsDao: GameSettingsDao

    /**
     * Sets up the test environment before each test.
     * This method creates a fresh in-memory database instance for each test case
     * to ensure test isolation.
     */
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        playerDao = db.playerDao()
        settingsDao = db.gameSettingsDao()
        Logger.instrumented("DaoTest: In-memory database created for test.")
    }

    /**
     * Cleans up the test environment after each test.
     * This method closes the database connection to release resources.
     */
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
        Logger.instrumented("DaoTest: In-memory database closed.")
    }

    /**
     * Tests that saving and then retrieving settings works correctly.
     */
    @Test
    @Throws(Exception::class)
    fun settingsDao_saveAndGetSettings() = runTest {
        Logger.instrumented("TEST_START: settingsDao_saveAndGetSettings")
        // Arrange
        val newSettings = GameSettings(playerCount = 4, startingLife = 20)
        Logger.instrumented("Arrange: Settings object created (playerCount=4, startingLife=20).")

        // Act
        Logger.instrumented("Act: Saving settings.")
        settingsDao.saveSettings(newSettings)
        val retrievedSettings = settingsDao.getSettings().first()
        Logger.instrumented("Act: Retrieved settings: $retrievedSettings")

        // Assert
        Logger.instrumented("Assert: Verifying retrieved settings.")
        assertEquals(4, retrievedSettings?.playerCount)
        assertEquals(20, retrievedSettings?.startingLife)
        Logger.instrumented("TEST_PASS: settingsDao_saveAndGetSettings")
    }

    /**
     * Tests that inserting and retrieving a list of players works correctly.
     */
    @Test
    @Throws(Exception::class)
    fun playerDao_insertAndGetPlayers() = runTest {
        Logger.instrumented("TEST_START: playerDao_insertAndGetPlayers")
        // Arrange
        val players = listOf(
            Player(gameSize = 2, playerIndex = 0, name = "Player 1", life = 40),
            Player(gameSize = 2, playerIndex = 1, name = "Player 2", life = 40)
        )
        Logger.instrumented("Arrange: List of 2 players created.")

        // Act
        Logger.instrumented("Act: Inserting players.")
        playerDao.insertAll(players)
        val retrievedPlayers = playerDao.getPlayers(2).first()
        Logger.instrumented("Act: Retrieved ${retrievedPlayers.size} players for game size 2.")

        // Assert
        Logger.instrumented("Assert: Verifying player list.")
        assertEquals(2, retrievedPlayers.size)
        assertEquals("Player 1", retrievedPlayers[0].name)
        Logger.instrumented("TEST_PASS: playerDao_insertAndGetPlayers")
    }

    /**
     * Tests that updating a player's data is correctly persisted.
     */
    @Test
    @Throws(Exception::class)
    fun playerDao_updatePlayer() = runTest {
        Logger.instrumented("TEST_START: playerDao_updatePlayer")
        // Arrange
        val player = Player(gameSize = 2, playerIndex = 0, life = 40)
        playerDao.insertAll(listOf(player))
        Logger.instrumented("Arrange: Inserted player with life 40.")
        val updatedPlayer = player.copy(life = 35)

        // Act
        Logger.instrumented("Act: Updating player's life to 35.")
        playerDao.updatePlayer(updatedPlayer)
        val retrievedPlayers = playerDao.getPlayers(2).first()

        // Assert
        Logger.instrumented("Assert: Verifying player's life is updated.")
        assertEquals(35, retrievedPlayers[0].life)
        Logger.instrumented("TEST_PASS: playerDao_updatePlayer")
    }

    /**
     * Tests that deleting players for a specific game size works as expected.
     */
    @Test
    @Throws(Exception::class)
    fun playerDao_deletePlayersForGame() = runTest {
        Logger.instrumented("TEST_START: playerDao_deletePlayersForGame")
        // Arrange
        val players = listOf(
            Player(gameSize = 2, playerIndex = 0, life = 40),
            Player(gameSize = 4, playerIndex = 0, life = 20)
        )
        playerDao.insertAll(players)
        Logger.instrumented("Arrange: Inserted players for game sizes 2 and 4.")

        // Act
        Logger.instrumented("Act: Deleting players for game size 2.")
        playerDao.deletePlayersForGame(gameSize = 2)
        val playersForGame2 = playerDao.getPlayers(2).first()
        val playersForGame4 = playerDao.getPlayers(4).first()

        // Assert
        Logger.instrumented("Assert: Verifying game size 2 is empty and game size 4 remains.")
        assertTrue(playersForGame2.isEmpty())
        assertEquals(1, playersForGame4.size)
        Logger.instrumented("TEST_PASS: playerDao_deletePlayersForGame")
    }

    /**
     * Tests that the deleteAll operation removes all players from the table.
     */
    @Test
    @Throws(Exception::class)
    fun playerDao_deleteAll() = runTest {
        Logger.instrumented("TEST_START: playerDao_deleteAll")
        // Arrange
        val players = listOf(
            Player(gameSize = 2, playerIndex = 0, life = 40),
            Player(gameSize = 4, playerIndex = 0, life = 20)
        )
        playerDao.insertAll(players)
        Logger.instrumented("Arrange: Inserted players for game sizes 2 and 4.")

        // Act
        Logger.instrumented("Act: Deleting all players.")
        playerDao.deleteAll()
        val retrievedPlayers = playerDao.getAllPlayers().first()

        // Assert
        Logger.instrumented("Assert: Verifying players table is empty.")
        assertTrue(retrievedPlayers.isEmpty())
        Logger.instrumented("TEST_PASS: playerDao_deleteAll")
    }
}