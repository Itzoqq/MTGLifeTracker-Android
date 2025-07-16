package com.example.mtglifetracker

import androidx.test.platform.app.InstrumentationRegistry
import com.example.mtglifetracker.util.Logger
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [org.junit.rules.TestRule] that disables animations on the device before a test runs
 * and re-enables them after it finishes.
 *
 * Animations can cause significant flakiness in UI tests because they introduce
 * unpredictable delays. Espresso may try to interact with a view that is still
 * animating into place, causing the test to fail. By disabling all window,
 * transition, and animator scaling, we ensure the UI is in a static and predictable
 * state, leading to more stable and reliable tests.
 *
 * ### Usage
 * In your test class, declare this rule with an order that runs *after* the Hilt rule
 * but *before* the Activity rule.
 * ```
 * @get:Rule(order = 1)
 * val disableAnimationsRule = DisableAnimationsRule()
 * ```
 */
class DisableAnimationsRule : TestWatcher() {

    /**
     * Called by the JUnit runner before each test method starts.
     * This method calls the helper to set all animation scales to 0, effectively
     * disabling them.
     */
    override fun starting(description: Description) {
        super.starting(description)
        Logger.instrumented("DisableAnimationsRule: Disabling all device animations for test '${description.methodName}'.")
        setAnimationScales(0.0f)
    }

    /**
     * Called by the JUnit runner after each test method finishes, regardless of
     * whether it passed or failed. This method restores the animation scales to their
     * default value of 1.
     */
    override fun finished(description: Description) {
        super.finished(description)
        Logger.instrumented("DisableAnimationsRule: Re-enabling all device animations after test '${description.methodName}'.")
        setAnimationScales(1.0f)
    }

    /**
     * A helper method that executes shell commands on the device to change the global
     * animation scale settings.
     *
     * @param scale The animation scale to set. `0.0f` disables animations, `1.0f` is the default.
     */
    private fun setAnimationScales(scale: Float) {
        Logger.instrumented("DisableAnimationsRule: Setting animation scales to $scale.")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // These commands change the system-wide settings for animations.
        instrumentation.uiAutomation.executeShellCommand("settings put global window_animation_scale $scale")
        instrumentation.uiAutomation.executeShellCommand("settings put global transition_animation_scale $scale")
        instrumentation.uiAutomation.executeShellCommand("settings put global animator_duration_scale $scale")
    }
}