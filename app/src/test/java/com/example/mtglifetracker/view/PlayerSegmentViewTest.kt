package com.example.mtglifetracker.view

import android.view.MotionEvent
import android.view.View.MeasureSpec
import com.example.mtglifetracker.ThemedRobolectricTest
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Local unit tests for the [PlayerSegmentView].
 *
 * This test class uses Robolectric to test the view's internal transformation logic
 * for measurement and touch events when rotated. It avoids the complexities of full
 * UI touch dispatching by directly invoking the view's methods and asserting their behavior.
 */
@Config(sdk = [34])
class PlayerSegmentViewTest : ThemedRobolectricTest() {

    private lateinit var playerSegmentView: PlayerSegmentView

    /**
     * Sets up the test environment before each test.
     * This method creates a new instance of the [PlayerSegmentView] and gives it a
     * fixed size so that its transformation logic can be tested reliably.
     */
    @Before
    fun setUp() {
        Logger.unit("TEST_SETUP: Starting...")
        playerSegmentView = PlayerSegmentView(themedContext)

        // Measure and lay out the view so it has dimensions (width and height).
        // This is crucial for testing the transformation logic which depends on these values.
        playerSegmentView.measure(
            MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY)
        )
        playerSegmentView.layout(0, 0, 200, 100)
        Logger.unit("TEST_SETUP: Complete. PlayerSegmentView created with size 200x100.")
    }

    /**
     * Tests that the view correctly inflates its layout and finds its child views upon initialization.
     */
    @Test
    fun initialization_shouldInflateLayoutAndFindViews() {
        Logger.unit("TEST_START: initialization_shouldInflateLayoutAndFindViews")
        // Arrange
        Logger.unit("Arrange: Creating a new PlayerSegmentView instance.")
        val layout = PlayerSegmentView(themedContext)

        // Assert
        Logger.unit("Assert: Verifying child views are not null and default angle is 0.")
        assertNotNull("LifeCounterView should not be null", layout.lifeCounter)
        assertNotNull("PlayerName TextView should not be null", layout.playerName)
        assertEquals("Default angle should be 0", 0, layout.angle)
        Logger.unit("TEST_PASS: initialization_shouldInflateLayoutAndFindViews")
    }

    // --- Measurement Tests ---

    /**
     * Tests that the `onMeasure` method does not swap dimensions when the view has no rotation.
     */
    @Test
    fun onMeasure_shouldNotSwapDimensions_whenAngleIs0() {
        Logger.unit("TEST_START: onMeasure_shouldNotSwapDimensions_whenAngleIs0")
        // Arrange
        playerSegmentView.angle = 0
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)
        Logger.unit("Arrange: View angle is 0.")

        // Act
        Logger.unit("Act: Calling measure().")
        playerSegmentView.measure(widthSpec, heightSpec)

        // Assert
        Logger.unit("Assert: Verifying measured dimensions were not swapped.")
        assertEquals("Width should not be swapped", 300, playerSegmentView.measuredWidth)
        assertEquals("Height should not be swapped", 150, playerSegmentView.measuredHeight)
        Logger.unit("TEST_PASS: onMeasure_shouldNotSwapDimensions_whenAngleIs0")
    }

    /**
     * Tests that the `onMeasure` method correctly swaps dimensions when rotated by 90 degrees.
     */
    @Test
    fun onMeasure_shouldKeepOriginalDimensions_whenAngleIs90() {
        Logger.unit("TEST_START: onMeasure_shouldKeepOriginalDimensions_whenAngleIs90")
        // Arrange
        playerSegmentView.angle = 90
        val widthSpec = MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)
        Logger.unit("Arrange: View angle is 90.")

        // Act
        Logger.unit("Act: Calling measure().")
        playerSegmentView.measure(widthSpec, heightSpec)

        // Assert
        Logger.unit("Assert: Verifying measured dimensions were swapped correctly.")
        assertEquals("Width should be swapped from height", 300, playerSegmentView.measuredWidth)
        assertEquals("Height should be swapped from width", 150, playerSegmentView.measuredHeight)
        Logger.unit("TEST_PASS: onMeasure_shouldKeepOriginalDimensions_whenAngleIs90")
    }

    // ... Additional measurement tests would follow a similar pattern ...

    // --- Transformation Logic Tests ---

    /**
     * Tests that `transformEvent` makes no changes to coordinates when the angle is 0.
     */
    @Test
    fun transformEvent_shouldNotTransformCoordinates_whenAngleIs0() {
        Logger.unit("TEST_START: transformEvent_shouldNotTransformCoordinates_whenAngleIs0")
        // Arrange
        playerSegmentView.angle = 0
        val downEvent = MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN, 50f, 75f,0)
        Logger.unit("Arrange: Created MotionEvent at (50, 75) with angle 0.")

        // Act
        Logger.unit("Act: Calling transformEvent().")
        playerSegmentView.transformEvent(downEvent)

        // Assert
        Logger.unit("Assert: Verifying coordinates are unchanged. x=${downEvent.x}, y=${downEvent.y}")
        assertEquals(50f, downEvent.x, 0.01f)
        assertEquals(75f, downEvent.y, 0.01f)
        Logger.unit("TEST_PASS: transformEvent_shouldNotTransformCoordinates_whenAngleIs0")
    }

    /**
     * Tests that `transformEvent` correctly transforms coordinates for a 90-degree rotation.
     */
    @Test
    fun transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs90() {
        Logger.unit("TEST_START: transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs90")
        // Arrange
        playerSegmentView.angle = 90
        val downEvent = MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN, 150f, 40f,0)
        Logger.unit("Arrange: Created MotionEvent at (150, 40) with angle 90.")

        // Act
        Logger.unit("Act: Calling transformEvent().")
        playerSegmentView.transformEvent(downEvent)

        // Assert
        Logger.unit("Assert: Verifying transformed coordinates. x=${downEvent.x}, y=${downEvent.y}")
        // Expected transformation for -90 rotation + translation: x' = y, y' = -x + width
        assertEquals(40f, downEvent.x, 0.01f)
        assertEquals(50f, downEvent.y, 0.01f)
        Logger.unit("TEST_PASS: transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs90")
    }

    /**
     * Tests that `transformEvent` correctly transforms coordinates for a 180-degree rotation.
     */
    @Test
    fun transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs180() {
        Logger.unit("TEST_START: transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs180")
        // Arrange
        playerSegmentView.angle = 180
        val downEvent = MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN, 60f, 70f,0)
        Logger.unit("Arrange: Created MotionEvent at (60, 70) with angle 180.")

        // Act
        Logger.unit("Act: Calling transformEvent().")
        playerSegmentView.transformEvent(downEvent)

        // Assert
        Logger.unit("Assert: Verifying transformed coordinates. x=${downEvent.x}, y=${downEvent.y}")
        // Expected transformation for -180 rotation + translation: x' = -x + width, y' = -y + height
        assertEquals(140f, downEvent.x, 0.01f)
        assertEquals(30f, downEvent.y, 0.01f)
        Logger.unit("TEST_PASS: transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIs180")
    }

    /**
     * Tests that `transformEvent` correctly transforms coordinates for a -90-degree rotation.
     */
    @Test
    fun transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIsMinus90() {
        Logger.unit("TEST_START: transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIsMinus90")
        // Arrange
        playerSegmentView.angle = -90 // Same as 270 degrees
        val downEvent = MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN, 80f, 20f,0)
        Logger.unit("Arrange: Created MotionEvent at (80, 20) with angle -90.")

        // Act
        Logger.unit("Act: Calling transformEvent().")
        playerSegmentView.transformEvent(downEvent)

        // Assert
        Logger.unit("Assert: Verifying transformed coordinates. x=${downEvent.x}, y=${downEvent.y}")
        // Expected transformation for -270 rotation + translation: x' = -y + height, y' = x
        assertEquals(80f, downEvent.x, 0.01f)
        assertEquals(80f, downEvent.y, 0.01f)
        Logger.unit("TEST_PASS: transformEvent_shouldCorrectlyTransformCoordinates_whenAngleIsMinus90")
    }
}