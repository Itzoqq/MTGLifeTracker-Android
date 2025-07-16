package com.example.mtglifetracker.view

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mtglifetracker.ThemedRobolectricTest
import com.example.mtglifetracker.util.Logger
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Local unit tests for the [PlayerLayoutManager].
 *
 * This class uses Robolectric to allow the instantiation and manipulation of Android
 * Framework classes (like [ConstraintLayout]) in a local JVM environment. Each test
 * verifies that the [PlayerLayoutManager] correctly creates the right number of
 * player segments and dividers, and assigns the correct rotation angles for each
 * supported player count.
 */
// No @RunWith here, it's inherited from ThemedRobolectricTest
@Config(sdk = [34])
class PlayerLayoutManagerTest : ThemedRobolectricTest() {

    private lateinit var container: ConstraintLayout
    private lateinit var playerLayoutManager: PlayerLayoutManager

    /**
     * Sets up the test environment before each test.
     * This method creates a new container and a new instance of the [PlayerLayoutManager]
     * to ensure each test is isolated.
     */
    @Before
    fun setUp() {
        Logger.unit("TEST_SETUP: Starting...")
        // themedContext is provided by the ThemedRobolectricTest base class
        container = ConstraintLayout(themedContext)
        playerLayoutManager = PlayerLayoutManager(container, themedContext)
        Logger.unit("TEST_SETUP: Complete. PlayerLayoutManager is ready.")
    }

    /**
     * Tests the layout creation for a 2-player game.
     */
    @Test
    fun createPlayerLayouts_for2Players_shouldAdd2SegmentsAnd1Divider() {
        Logger.unit("TEST_START: createPlayerLayouts_for2Players_shouldAdd2SegmentsAnd1Divider")
        // Act
        Logger.unit("Act: Calling createPlayerLayouts(2).")
        playerLayoutManager.createPlayerLayouts(2)

        // Assert
        Logger.unit("Assert: Verifying segment count, child count, and angles.")
        assertEquals("Should create 2 player segments", 2, playerLayoutManager.playerSegments.size)
        // 2 player segments + 1 horizontal divider = 3 total children
        assertEquals("Should have 3 total views in container", 3, container.childCount)
        assertEquals("Player 1 angle should be 180", 180, playerLayoutManager.playerSegments[0].angle)
        assertEquals("Player 2 angle should be 0", 0, playerLayoutManager.playerSegments[1].angle)
        Logger.unit("TEST_PASS: createPlayerLayouts_for2Players_shouldAdd2SegmentsAnd1Divider")
    }

    /**
     * Tests the layout creation for a 3-player game.
     */
    @Test
    fun createPlayerLayouts_for3Players_shouldAdd3SegmentsAnd2Dividers() {
        Logger.unit("TEST_START: createPlayerLayouts_for3Players_shouldAdd3SegmentsAnd2Dividers")
        // Act
        Logger.unit("Act: Calling createPlayerLayouts(3).")
        playerLayoutManager.createPlayerLayouts(3)

        // Assert
        Logger.unit("Assert: Verifying segment count, child count, and angles.")
        assertEquals("Should create 3 player segments", 3, playerLayoutManager.playerSegments.size)
        // 3 player segments + 2 dividers (1h, 1v) = 5 total children
        assertEquals("Should have 5 total views in container", 5, container.childCount)
        assertEquals("Player 1 angle should be 180", 180, playerLayoutManager.playerSegments[0].angle)
        assertEquals("Player 2 angle should be 90", 90, playerLayoutManager.playerSegments[1].angle)
        assertEquals("Player 3 angle should be -90", -90, playerLayoutManager.playerSegments[2].angle)
        Logger.unit("TEST_PASS: createPlayerLayouts_for3Players_shouldAdd3SegmentsAnd2Dividers")
    }

    /**
     * Tests the layout creation for a 4-player game.
     */
    @Test
    fun createPlayerLayouts_for4Players_shouldAdd4SegmentsAnd2Dividers() {
        Logger.unit("TEST_START: createPlayerLayouts_for4Players_shouldAdd4SegmentsAnd2Dividers")
        // Act
        Logger.unit("Act: Calling createPlayerLayouts(4).")
        playerLayoutManager.createPlayerLayouts(4)

        // Assert
        Logger.unit("Assert: Verifying segment count, child count, and angles.")
        assertEquals("Should create 4 player segments", 4, playerLayoutManager.playerSegments.size)
        // 4 player segments + 2 dividers (1h, 1v) = 6 total children
        assertEquals("Should have 6 total views in container", 6, container.childCount)
        assertEquals("Player 1 angle should be 90", 90, playerLayoutManager.playerSegments[0].angle)
        assertEquals("Player 2 angle should be -90", -90, playerLayoutManager.playerSegments[1].angle)
        assertEquals("Player 3 angle should be 90", 90, playerLayoutManager.playerSegments[2].angle)
        assertEquals("Player 4 angle should be -90", -90, playerLayoutManager.playerSegments[3].angle)
        Logger.unit("TEST_PASS: createPlayerLayouts_for4Players_shouldAdd4SegmentsAnd2Dividers")
    }

    /**
     * Tests the layout creation for a 5-player game.
     */
    @Test
    fun createPlayerLayouts_for5Players_shouldAdd5SegmentsAnd4Dividers() {
        Logger.unit("TEST_START: createPlayerLayouts_for5Players_shouldAdd5SegmentsAnd4Dividers")
        // Act
        Logger.unit("Act: Calling createPlayerLayouts(5).")
        playerLayoutManager.createPlayerLayouts(5)

        // Assert
        Logger.unit("Assert: Verifying segment count, child count, and angles.")
        assertEquals("Should create 5 player segments", 5, playerLayoutManager.playerSegments.size)
        // 5 player segments + 4 dividers (1v, 3h) = 9 total children
        assertEquals("Should have 9 total views in container", 9, container.childCount)
        assertEquals("Player 1 angle should be 90", 90, playerLayoutManager.playerSegments[0].angle)
        assertEquals("Player 2 angle should be 90", 90, playerLayoutManager.playerSegments[1].angle)
        assertEquals("Player 3 angle should be -90", -90, playerLayoutManager.playerSegments[2].angle)
        assertEquals("Player 4 angle should be -90", -90, playerLayoutManager.playerSegments[3].angle)
        assertEquals("Player 5 angle should be -90", -90, playerLayoutManager.playerSegments[4].angle)
        Logger.unit("TEST_PASS: createPlayerLayouts_for5Players_shouldAdd5SegmentsAnd4Dividers")
    }

    /**
     * Tests the layout creation for a 6-player game.
     */
    @Test
    fun createPlayerLayouts_for6Players_shouldAdd6SegmentsAnd3Dividers() {
        Logger.unit("TEST_START: createPlayerLayouts_for6Players_shouldAdd6SegmentsAnd3Dividers")
        // Act
        Logger.unit("Act: Calling createPlayerLayouts(6).")
        playerLayoutManager.createPlayerLayouts(6)

        // Assert
        Logger.unit("Assert: Verifying segment count, child count, and angles.")
        assertEquals("Should create 6 player segments", 6, playerLayoutManager.playerSegments.size)
        // 6 player segments + 3 dividers (1v, 2h) = 9 total children
        assertEquals("Should have 9 total views in container", 9, container.childCount)
        assertEquals("Player 1 angle should be 90", 90, playerLayoutManager.playerSegments[0].angle)
        assertEquals("Player 2 angle should be -90", -90, playerLayoutManager.playerSegments[1].angle)
        assertEquals("Player 3 angle should be 90", 90, playerLayoutManager.playerSegments[2].angle)
        assertEquals("Player 4 angle should be -90", -90, playerLayoutManager.playerSegments[3].angle)
        assertEquals("Player 5 angle should be 90", 90, playerLayoutManager.playerSegments[4].angle)
        assertEquals("Player 6 angle should be -90", -90, playerLayoutManager.playerSegments[5].angle)
        Logger.unit("TEST_PASS: createPlayerLayouts_for6Players_shouldAdd6SegmentsAnd3Dividers")
    }
}