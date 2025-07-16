package com.example.mtglifetracker.view

import android.content.Context
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.example.mtglifetracker.createMotionEvent
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.Duration

/**
 * Local unit tests for the [LifeCounterView].
 *
 * This class uses Robolectric to test the view's complex touch handling logic in a simulated
 * Android environment. It verifies that single taps and long-presses on different halves of
 * the view correctly trigger the appropriate listener callbacks. It uses mock listeners to
 * verify interactions and `Shadows.shadowOf(Looper.getMainLooper())` to control the clock
 * for time-based tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LifeCounterViewTest {

    private lateinit var lifeCounterView: LifeCounterView
    // Mock listeners to verify that the view's callbacks are invoked correctly.
    private lateinit var mockIncreaseListener: () -> Unit
    private lateinit var mockDecreaseListener: () -> Unit

    /**
     * Sets up the test environment before each test.
     * This method creates an instance of the [LifeCounterView], gives it a measured size,
     * and initializes the mock listeners.
     */
    @Before
    fun setUp() {
        Logger.unit("TEST_SETUP: Starting...")
        val context: Context = ApplicationProvider.getApplicationContext()
        lifeCounterView = LifeCounterView(context)

        // Manually measure and lay out the view so it has a width and height for hit-testing.
        lifeCounterView.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY)
        )
        lifeCounterView.layout(0, 0, 200, 100)

        // Initialize mock listeners and attach them to the view.
        mockIncreaseListener = mock()
        mockDecreaseListener = mock()
        lifeCounterView.onLifeIncreasedListener = mockIncreaseListener
        lifeCounterView.onLifeDecreasedListener = mockDecreaseListener
        Logger.unit("TEST_SETUP: Complete. LifeCounterView is ready.")
    }

    /**
     * Tests that a single, short tap on the right half of the view triggers the increase listener exactly once.
     */
    @Test
    fun singleTapOnRightHalf_shouldTriggerIncreaseListenerOnce() {
        Logger.unit("TEST_START: singleTapOnRightHalf_shouldTriggerIncreaseListenerOnce")
        // Act: Simulate a quick tap on the right side of the view (x=150 in a 200-width view).
        Logger.unit("Act: Simulating tap on right half of the view.")
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 150f, 50f))

        // Assert: Verify the increase listener was called once and the decrease listener was never called.
        Logger.unit("Assert: Verifying increase listener was called once.")
        verify(mockIncreaseListener, times(1)).invoke()
        verify(mockDecreaseListener, never()).invoke()
        Logger.unit("TEST_PASS: singleTapOnRightHalf_shouldTriggerIncreaseListenerOnce")
    }

    /**
     * Tests that a single, short tap on the left half of the view triggers the decrease listener exactly once.
     */
    @Test
    fun singleTapOnLeftHalf_shouldTriggerDecreaseListenerOnce() {
        Logger.unit("TEST_START: singleTapOnLeftHalf_shouldTriggerDecreaseListenerOnce")
        // Act: Simulate a quick tap on the left side of the view (x=50 in a 200-width view).
        Logger.unit("Act: Simulating tap on left half of the view.")
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 50f))
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 50f, 50f))

        // Assert: Verify the decrease listener was called once and the increase listener was never called.
        Logger.unit("Assert: Verifying decrease listener was called once.")
        verify(mockDecreaseListener, times(1)).invoke()
        verify(mockIncreaseListener, never()).invoke()
        Logger.unit("TEST_PASS: singleTapOnLeftHalf_shouldTriggerDecreaseListenerOnce")
    }

    /**
     * Tests that pressing and holding on the right half triggers the increase listener multiple times.
     */
    @Test
    fun pressAndHoldOnRightHalf_shouldTriggerIncreaseListenerMultipleTimes() {
        Logger.unit("TEST_START: pressAndHoldOnRightHalf_shouldTriggerIncreaseListenerMultipleTimes")
        // Act: Simulate pressing down on the right half.
        Logger.unit("Act: Simulating press down on right half.")
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        verify(mockIncreaseListener, never()).invoke() // Nothing should happen immediately.

        // Advance the clock just past the initial delay (400ms).
        Logger.unit("Act: Advancing clock by 401ms to trigger first continuous event.")
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(401))
        // Assert: The listener should have been called exactly once.
        verify(mockIncreaseListener, times(1)).invoke()

        // Advance the clock further to trigger accelerated events.
        Logger.unit("Act: Advancing clock by another 500ms.")
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        // Assert: The listener should have been called at least 3 times in total.
        Logger.unit("Assert: Verifying listener was called multiple times.")
        verify(mockIncreaseListener, atLeast(3)).invoke()
        verify(mockDecreaseListener, never()).invoke()
        Logger.unit("TEST_PASS: pressAndHoldOnRightHalf_shouldTriggerIncreaseListenerMultipleTimes")
    }

    /**
     * Tests that pressing and holding on the left half triggers the decrease listener multiple times.
     */
    @Test
    fun pressAndHoldOnLeftHalf_shouldTriggerDecreaseListenerMultipleTimes() {
        Logger.unit("TEST_START: pressAndHoldOnLeftHalf_shouldTriggerDecreaseListenerMultipleTimes")
        // Act: Simulate pressing down on the left half.
        Logger.unit("Act: Simulating press down on left half.")
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 50f))
        verify(mockDecreaseListener, never()).invoke()

        // Advance the clock past the initial delay.
        Logger.unit("Act: Advancing clock by 401ms.")
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(401))
        verify(mockDecreaseListener, times(1)).invoke()

        // Advance the clock further.
        Logger.unit("Act: Advancing clock by another 500ms.")
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        // Assert: The listener should have been called multiple times.
        Logger.unit("Assert: Verifying listener was called multiple times.")
        verify(mockDecreaseListener, atLeast(3)).invoke()
        verify(mockIncreaseListener, never()).invoke()
        Logger.unit("TEST_PASS: pressAndHoldOnLeftHalf_shouldTriggerDecreaseListenerMultipleTimes")
    }

    /**
     * Tests that releasing the touch stops the continuous updates.
     */
    @Test
    fun releasingFinger_shouldStopContinuousUpdates() {
        Logger.unit("TEST_START: releasingFinger_shouldStopContinuousUpdates")
        val shadowLooper = Shadows.shadowOf(Looper.getMainLooper())
        val downTime = 0L
        val pressDuration = 401L

        // Act: Press down and hold for long enough to trigger one continuous update.
        Logger.unit("Act: Pressing down and advancing clock by 401ms.")
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 150f, 50f, 0)
        lifeCounterView.dispatchTouchEvent(downEvent)
        downEvent.recycle()
        shadowLooper.idleFor(Duration.ofMillis(pressDuration))
        verify(mockIncreaseListener, times(1)).invoke()

        // Act: Release the touch.
        Logger.unit("Act: Releasing touch.")
        val upEventTime = downTime + pressDuration
        val upEvent = MotionEvent.obtain(downTime, upEventTime, MotionEvent.ACTION_UP, 150f, 50f, 0)
        lifeCounterView.dispatchTouchEvent(upEvent)
        upEvent.recycle()

        // Act: Advance the clock significantly further.
        Logger.unit("Act: Advancing clock by 2 seconds to ensure no more events fire.")
        shadowLooper.idleFor(Duration.ofSeconds(2))
        // Assert: The listener should still have only been called once.
        Logger.unit("Assert: Verifying listener was not called again after release.")
        verify(mockIncreaseListener, times(1)).invoke()
        Logger.unit("TEST_PASS: releasingFinger_shouldStopContinuousUpdates")
    }

    /**
     * Tests that a touch event dismisses a visible overlay instead of changing the life total.
     */
    @Test
    fun touchEvent_whenOverlayIsVisible_shouldDismissOverlayAndNotChangeLife() {
        Logger.unit("TEST_START: touchEvent_whenOverlayIsVisible_shouldDismissOverlayAndNotChangeLife")
        // Arrange: Create a mock overlay, make it visible, and register it with the view.
        val mockOverlay = View(ApplicationProvider.getApplicationContext())
        mockOverlay.visibility = View.VISIBLE
        lifeCounterView.addDismissibleOverlay(mockOverlay)
        Logger.unit("Arrange: Mock overlay created and made visible.")

        // Act: Simulate a tap on the view.
        Logger.unit("Act: Simulating tap on the view.")
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 150f, 50f))

        // Assert
        Logger.unit("Assert: Verifying overlay was hidden and life did not change.")
        // Verify the overlay was hidden.
        assertEquals(View.GONE, mockOverlay.visibility)
        // Verify that life change listeners were NOT called.
        verify(mockIncreaseListener, never()).invoke()
        verify(mockDecreaseListener, never()).invoke()
        Logger.unit("TEST_PASS: touchEvent_whenOverlayIsVisible_shouldDismissOverlayAndNotChangeLife")
    }
}