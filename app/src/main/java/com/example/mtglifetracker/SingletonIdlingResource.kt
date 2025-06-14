package com.example.mtglifetracker

import androidx.test.espresso.idling.CountingIdlingResource

/**
 * A singleton wrapper for CountingIdlingResource to be used in both app and test code.
 */
object SingletonIdlingResource {
    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}