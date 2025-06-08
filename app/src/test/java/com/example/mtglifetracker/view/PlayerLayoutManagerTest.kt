package com.example.mtglifetracker.view

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for the PlayerLayoutManager.
 *
 * This class uses Robolectric to test the view creation and constraint logic
 * for each supported player count, ensuring the layout is constructed correctly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Use a supported SDK version for Robolectric
class PlayerLayoutManagerTest {

    private lateinit var context: Context
    private lateinit var container: ConstraintLayout
    private lateinit var playerLayoutManager: PlayerLayoutManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        container = ConstraintLayout(context)
        playerLayoutManager = PlayerLayoutManager(container, context)
    }

    @Test
    fun `createPlayerLayouts for 2 players should add 2 segments and 1 divider`() {
        // Act
        playerLayoutManager.createPlayerLayouts(2)

        // Assert
        assertEquals("Should create 2 player segments", 2, playerLayoutManager.playerSegments.size)
        // 2 player segments + 1 horizontal divider = 3 total children
        assertEquals("Should have 3 total views in container", 3, container.childCount)
        assertEquals(
            "Player 1 angle should be 180",
            180,
            playerLayoutManager.playerSegments[0].angle
        )
        assertEquals("Player 2 angle should be 0", 0, playerLayoutManager.playerSegments[1].angle)
    }

    @Test
    fun `createPlayerLayouts for 3 players should add 3 segments and 2 dividers`() {
        // Act
        playerLayoutManager.createPlayerLayouts(3)

        // Assert
        assertEquals("Should create 3 player segments", 3, playerLayoutManager.playerSegments.size)
        // 3 player segments + 2 dividers (1h, 1v) = 5 total children
        assertEquals("Should have 5 total views in container", 5, container.childCount)
        assertEquals(
            "Player 1 angle should be 180",
            180,
            playerLayoutManager.playerSegments[0].angle
        )
        assertEquals("Player 2 angle should be 90", 90, playerLayoutManager.playerSegments[1].angle)
        assertEquals(
            "Player 3 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[2].angle
        )
    }

    @Test
    fun `createPlayerLayouts for 4 players should add 4 segments and 2 dividers`() {
        // Act
        playerLayoutManager.createPlayerLayouts(4)

        // Assert
        assertEquals("Should create 4 player segments", 4, playerLayoutManager.playerSegments.size)
        // 4 player segments + 2 dividers (1h, 1v) = 6 total children
        assertEquals("Should have 6 total views in container", 6, container.childCount)
        assertEquals("Player 1 angle should be 90", 90, playerLayoutManager.playerSegments[0].angle)
        assertEquals(
            "Player 2 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[1].angle
        )
        assertEquals("Player 3 angle should be 90", 90, playerLayoutManager.playerSegments[2].angle)
        assertEquals(
            "Player 4 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[3].angle
        )
    }

    @Test
    fun `createPlayerLayouts for 5 players should add 5 segments and 4 dividers`() {
        // Act
        playerLayoutManager.createPlayerLayouts(5)

        // Assert
        assertEquals("Should create 5 player segments", 5, playerLayoutManager.playerSegments.size)
        // 5 player segments + 4 dividers (1v, 3h) = 9 total children
        assertEquals("Should have 9 total views in container", 9, container.childCount)
        assertEquals("Player 1 angle should be 90", 90, playerLayoutManager.playerSegments[0].angle)
        assertEquals("Player 2 angle should be 90", 90, playerLayoutManager.playerSegments[1].angle)
        assertEquals(
            "Player 3 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[2].angle
        )
        assertEquals(
            "Player 4 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[3].angle
        )
        assertEquals(
            "Player 5 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[4].angle
        )
    }

    @Test
    fun `createPlayerLayouts for 6 players should add 6 segments and 3 dividers`() {
        // Act
        playerLayoutManager.createPlayerLayouts(6)

        // Assert
        assertEquals("Should create 6 player segments", 6, playerLayoutManager.playerSegments.size)
        // 6 player segments + 3 dividers (1v, 2h) = 9 total children
        assertEquals("Should have 9 total views in container", 9, container.childCount)
        assertEquals("Player 1 angle should be 90", 90, playerLayoutManager.playerSegments[0].angle)
        assertEquals(
            "Player 2 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[1].angle
        )
        assertEquals("Player 3 angle should be 90", 90, playerLayoutManager.playerSegments[2].angle)
        assertEquals(
            "Player 4 angle should be -90",
            -90,
            playerLayoutManager.playerSegments[3].angle
        )
        assertEquals("Player 5 angle should be 90", 90, playerLayoutManager.playerSegments[4].angle)
        assertEquals(
            "Player 6 angle should be 90",
            -90,
            playerLayoutManager.playerSegments[5].angle
        )
    }
}