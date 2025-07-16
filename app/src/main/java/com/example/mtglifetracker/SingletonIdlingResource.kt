package com.example.mtglifetracker

import androidx.test.espresso.idling.CountingIdlingResource
import com.example.mtglifetracker.util.Logger

/**
 * A singleton wrapper for [CountingIdlingResource] to be used in both app and test code.
 *
 * This object provides a globally accessible, thread-safe counter that Espresso can monitor
 * during instrumentation tests. When the counter is greater than zero, Espresso will wait.
 * When it is zero, Espresso will proceed with the next test action.
 *
 * This is crucial for synchronizing tests with long-running background tasks, such as
 * network requests, database operations, or complex UI updates that are driven by coroutines
 * or other asynchronous mechanisms.
 *
 * ### How it Works:
 * - **Increment:** Before starting a background task that the UI test should wait for, call `increment()`.
 * - **Decrement:** In a `finally` block or after the background task completes, call `decrement()`.
 *
 * This ensures that even if the task fails, the idling resource is always decremented,
 * preventing tests from hanging indefinitely.
 */
object SingletonIdlingResource {
    // A constant name for the resource, which will appear in test logs and timeout messages.
    private const val RESOURCE = "GLOBAL"

    /**
     * The actual [CountingIdlingResource] instance.
     * It is instantiated with a unique name to identify it in Espresso's output.
     * The `JvmField` annotation is used to expose this as a public field without a getter,
     * which is a common convention for static-like fields in Kotlin objects.
     */
    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    /**
     * Increments the counting idling resource, signaling that a long-running operation has started.
     * Espresso will now pause and wait until the counter returns to zero.
     */
    fun increment() {
        // Log the increment operation for easier debugging of test synchronization.
        Logger.d("IdlingResource: INCREMENTING counter. Current count will be: ${countingIdlingResource.getCounterVal() + 1}")
        countingIdlingResource.increment()
    }

    /**
     * Decrements the counting idling resource, signaling that a long-running operation has finished.
     * If the counter reaches zero, Espresso will resume test execution.
     * It is critical to call this for every call to `increment()` to avoid test timeouts.
     */
    fun decrement() {
        // Only log and decrement if the counter is not already idle, preventing potential errors.
        if (!countingIdlingResource.isIdleNow) {
            Logger.d("IdlingResource: DECREMENTING counter. Current count will be: ${countingIdlingResource.getCounterVal() - 1}")
            countingIdlingResource.decrement()
        } else {
            // This log can help identify logic errors where decrement is called too many times.
            Logger.w("IdlingResource: DECREMENT called when counter was already idle.")
        }
    }

    // Helper extension function to get the current value of the counter, useful for logging.
    private fun CountingIdlingResource.getCounterVal(): Int {
        // This is a workaround to get the internal counter value for logging purposes.
        // The field is not public, so we rely on its string representation.
        return this.toString().substringAfter("count=").substringBefore("}").toIntOrNull() ?: -1
    }
}