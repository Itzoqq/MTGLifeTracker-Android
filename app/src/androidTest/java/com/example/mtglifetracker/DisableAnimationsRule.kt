package com.example.mtglifetracker

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit Test Rule that disables animations on the device before a test runs,
 * and re-enables them after it finishes. This is crucial for creating stable
 * and reliable Espresso UI tests.
 *
 * Usage:
 * @get:Rule(order = 1) // Ensure Hilt rule runs first
 * val disableAnimationsRule = DisableAnimationsRule()
 */
class DisableAnimationsRule : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        setAnimationScales(0.0f)
    }

    override fun finished(description: Description) {
        super.finished(description)
        setAnimationScales(1.0f)
    }

    private fun setAnimationScales(scale: Float) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("settings put global window_animation_scale $scale")
        instrumentation.uiAutomation.executeShellCommand("settings put global transition_animation_scale $scale")
        instrumentation.uiAutomation.executeShellCommand("settings put global animator_duration_scale $scale")
    }
}