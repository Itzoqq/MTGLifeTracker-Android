package com.example.mtglifetracker.view

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mtglifetracker.ThemedRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Unit tests for the PlayerLayoutManager.
 *
 * This class uses Robolectric to test the view creation and constraint logic
 * for each supported player count, ensuring the layout is constructed correctly.
 */
// No @RunWith here, it's inherited from ThemedRobolectricTest
@Config(sdk = [34])
class PlayerLayoutManagerTest : ThemedRobolectricTest() { // Extends our base class

    private lateinit var container: ConstraintLayout
    private lateinit var playerLayoutManager: PlayerLayoutManager

    @Before
    fun setUp() {
        // themedContext is provided by the base class
        container = ConstraintLayout(themedContext)
        playerLayoutManager = PlayerLayoutManager(container, themedContext)
    }

    @Test
    fun createPlayerLayouts_for2Players_shouldAdd2SegmentsAnd1Divider() {
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
    fun createPlayerLayouts_for3Players_shouldAdd3SegmentsAnd2Dividers() {
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
    fun createPlayerLayouts_for4Players_shouldAdd4SegmentsAnd2Dividers() {
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
    fun createPlayerLayouts_for5Players_shouldAdd5SegmentsAnd4Dividers() {
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
    fun createPlayerLayouts_for6Players_shouldAdd6SegmentsAnd3Dividers() {
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