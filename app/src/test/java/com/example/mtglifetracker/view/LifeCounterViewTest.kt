package com.example.mtglifetracker.view

import android.content.Context
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LifeCounterViewTest {

    private lateinit var lifeCounterView: LifeCounterView
    private lateinit var mockIncreaseListener: () -> Unit
    private lateinit var mockDecreaseListener: () -> Unit

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        lifeCounterView = LifeCounterView(context)

        // Set the view's dimensions for accurate touch event testing
        lifeCounterView.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY)
        )
        lifeCounterView.layout(0, 0, 200, 100)

        mockIncreaseListener = mock()
        mockDecreaseListener = mock()

        lifeCounterView.onLifeIncreasedListener = mockIncreaseListener
        lifeCounterView.onLifeDecreasedListener = mockDecreaseListener
    }

    @Test
    fun singleTapOnRightHalf_shouldTriggerIncreaseListenerOnce() {
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 150f, 50f))

        verify(mockIncreaseListener, times(1)).invoke()
        verify(mockDecreaseListener, never()).invoke()
    }

    @Test
    fun singleTapOnLeftHalf_shouldTriggerDecreaseListenerOnce() {
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 50f))
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 50f, 50f))

        verify(mockDecreaseListener, times(1)).invoke()
        verify(mockIncreaseListener, never()).invoke()
    }

    @Test
    fun pressAndHoldOnRightHalf_shouldTriggerIncreaseListenerMultipleTimes() {
        // Press down on the right half of the view
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))

        // Verify that the listener is NOT called immediately
        verify(mockIncreaseListener, never()).invoke()

        // Advance the clock past the initial 400ms delay to trigger the first update
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(401))
        verify(mockIncreaseListener, times(1)).invoke()

        // Advance the clock further to trigger subsequent updates
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))

        // Check that the listener has been called multiple times
        verify(mockIncreaseListener, atLeast(3)).invoke()
        verify(mockDecreaseListener, never()).invoke()
    }

    @Test
    fun pressAndHoldOnLeftHalf_shouldTriggerDecreaseListenerMultipleTimes() {
        // Press down on the left half of the view
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 50f))

        // Verify that the listener is NOT called immediately
        verify(mockDecreaseListener, never()).invoke()

        // Advance the clock past the initial 400ms delay to trigger the first update
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(401))
        verify(mockDecreaseListener, times(1)).invoke()

        // Advance the clock further to trigger subsequent updates
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))

        // Check that the listener has been called multiple times
        verify(mockDecreaseListener, atLeast(3)).invoke()
        verify(mockIncreaseListener, never()).invoke()
    }

    @Test
    fun releasingFinger_shouldStopContinuousUpdates() {
        val shadowLooper = Shadows.shadowOf(Looper.getMainLooper())

        // Define our own start time and press duration for the test.
        // This avoids calling scheduler.currentTime, which is not allowed in PAUSED mode.
        val downTime = 0L
        val pressDuration = 401L

        // 1. Create and dispatch the ACTION_DOWN event using our defined downTime.
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 150f, 50f, 0)
        lifeCounterView.dispatchTouchEvent(downEvent)
        downEvent.recycle()

        // 2. Advance the Robolectric scheduler's clock by the press duration.
        shadowLooper.idleFor(Duration.ofMillis(pressDuration))
        verify(mockIncreaseListener, times(1)).invoke() // Verify the first long-press call

        // 3. Create the ACTION_UP event with an eventTime that reflects the duration.
        val upEventTime = downTime + pressDuration
        val upEvent = MotionEvent.obtain(downTime, upEventTime, MotionEvent.ACTION_UP, 150f, 50f, 0)
        lifeCounterView.dispatchTouchEvent(upEvent)
        upEvent.recycle()

        // 4. Advance the clock significantly to ensure no more updates occur.
        shadowLooper.idleFor(Duration.ofSeconds(2))

        // 5. Verify the listener was only ever called that one time.
        verify(mockIncreaseListener, times(1)).invoke()
    }

    @Suppress("SameParameterValue")
    private fun createMotionEvent(action: Int, x: Float, y: Float): MotionEvent {
        return MotionEvent.obtain(0, 0, action, x, y, 0)
    }
}