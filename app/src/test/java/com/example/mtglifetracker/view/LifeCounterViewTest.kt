package com.example.mtglifetracker.view

import android.content.Context
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.example.mtglifetracker.createMotionEvent
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.Duration
import junit.framework.TestCase.assertEquals

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
        verify(mockIncreaseListener, never()).invoke()

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(401))
        verify(mockIncreaseListener, times(1)).invoke()

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        verify(mockIncreaseListener, atLeast(3)).invoke()
        verify(mockDecreaseListener, never()).invoke()
    }

    @Test
    fun pressAndHoldOnLeftHalf_shouldTriggerDecreaseListenerMultipleTimes() {
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 50f, 50f))
        verify(mockDecreaseListener, never()).invoke()

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(401))
        verify(mockDecreaseListener, times(1)).invoke()

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        verify(mockDecreaseListener, atLeast(3)).invoke()
        verify(mockIncreaseListener, never()).invoke()
    }

    @Test
    fun releasingFinger_shouldStopContinuousUpdates() {
        val shadowLooper = Shadows.shadowOf(Looper.getMainLooper())
        val downTime = 0L
        val pressDuration = 401L

        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 150f, 50f, 0)
        lifeCounterView.dispatchTouchEvent(downEvent)
        downEvent.recycle()

        shadowLooper.idleFor(Duration.ofMillis(pressDuration))
        verify(mockIncreaseListener, times(1)).invoke()

        val upEventTime = downTime + pressDuration
        val upEvent = MotionEvent.obtain(downTime, upEventTime, MotionEvent.ACTION_UP, 150f, 50f, 0)
        lifeCounterView.dispatchTouchEvent(upEvent)
        upEvent.recycle()

        shadowLooper.idleFor(Duration.ofSeconds(2))
        verify(mockIncreaseListener, times(1)).invoke()
    }

    @Test
    fun touchEvent_whenOverlayIsVisible_shouldDismissOverlayAndNotChangeLife() {
        // Arrange
        val mockOverlay = View(ApplicationProvider.getApplicationContext())
        mockOverlay.visibility = View.VISIBLE
        lifeCounterView.addDismissibleOverlay(mockOverlay)

        // Act
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_DOWN, 150f, 50f))
        lifeCounterView.dispatchTouchEvent(createMotionEvent(MotionEvent.ACTION_UP, 150f, 50f))

        // Assert
        // Verify the overlay was hidden
        assertEquals(View.GONE, mockOverlay.visibility)
        // Verify that life change listeners were NOT called
        verify(mockIncreaseListener, never()).invoke()
        verify(mockDecreaseListener, never()).invoke()
    }

}