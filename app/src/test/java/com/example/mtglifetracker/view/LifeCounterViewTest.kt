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

/**
 * Unit tests for the LifeCounterView.
 * This class uses Robolectric to simulate the Android framework on the JVM.
 *
 * FIX: Added @Config(sdk = [34]) to resolve the targetSdkVersion mismatch.
 * Robolectric will now run the test simulating SDK 34.
 */
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
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        verify(mockIncreaseListener, times(1)).invoke() // First call is immediate

        // Use Robolectric's ShadowLooper to advance the clock
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(800))

        verify(mockIncreaseListener, atLeast(3)).invoke()
        verify(mockDecreaseListener, never()).invoke()
    }

    @Test
    fun pressAndHoldOnLeftHalf_shouldTriggerDecreaseListenerMultipleTimes() {
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 50f))
        verify(mockDecreaseListener, times(1)).invoke() // First call is immediate

        // Use Robolectric's ShadowLooper to advance the clock
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(800))

        verify(mockDecreaseListener, atLeast(3)).invoke()
        verify(mockIncreaseListener, never()).invoke()
    }

    @Test
    fun releasingFinger_shouldStopContinuousUpdates() {
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        verify(mockIncreaseListener, times(1)).invoke()

        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 150f, 50f))

        // Advance the clock significantly. If updates were still running,
        // the listener would be called again.
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))

        // Verify the listener was only ever called that initial time
        verify(mockIncreaseListener, times(1)).invoke()
    }

    @Suppress("SameParameterValue")
    private fun createMotionEvent(action: Int, x: Float, y: Float): MotionEvent {
        return MotionEvent.obtain(0, 0, action, x, y, 0)
    }
}
