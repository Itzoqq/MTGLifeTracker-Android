package com.example.mtglifetracker

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.example.mtglifetracker.util.Logger
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * An abstract base class for Robolectric tests that require the app's theme.
 *
 * Any test class extending this will automatically have two critical pieces of setup performed:
 * 1.  **Themed Context:** A `themedContext` property is made available. This is a context that
 * has the application's main theme (Theme.MTGLifeTracker) applied, which is essential
 * for correctly inflating and testing custom views that rely on theme attributes.
 * 2.  **Logging Configuration:** It configures the Timber logging library to output logs
 * to the standard console using `println`. This is necessary because local unit tests
 * run on a JVM and do not have access to Android's Logcat.
 *
 * By inheriting from this class, unit tests for views and other context-dependent classes
 * can be written more cleanly and reliably.
 */
@RunWith(RobolectricTestRunner::class)
abstract class ThemedRobolectricTest {

    /**
     * A [Context] with the app's main theme ([R.style.Theme_MTGLifeTracker]) applied.
     * This should be used when instantiating views under test to ensure they are themed correctly.
     */
    protected lateinit var themedContext: Context

    /**
     * A custom Timber [timber.log.Timber.Tree] that redirects all log messages to `println`.
     * This is the key component that allows a unified logging API (`Logger.unit`) to work
     * in local unit tests where Android's `Log` class is unavailable.
     */
    private class PrintlnTree : Timber.Tree() {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

        /**
         * The overridden log method that performs the actual printing.
         *
         * @param priority The log priority (e.g., Log.DEBUG). Not used here but required by the signature.
         * @param tag The tag for the log message (e.g., "UNIT_TEST").
         * @param message The actual log message.
         * @param t An optional throwable to print its stack trace.
         */
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val formattedTime = LocalDateTime.now().format(formatter)
            // Format the log message to include a timestamp and tag for readability.
            println("$formattedTime [${tag ?: "NO_TAG"}] $message")
            t?.printStackTrace()
        }
    }

    /**
     * Sets up the themed context and logging configuration before each test runs.
     * The `@Before` annotation ensures this method is executed prior to any `@Test` methods
     * in subclasses.
     */
    @Before
    fun setupThemeAndLogging() {
        Logger.unit("ThemedRobolectricTest: @Before - Setting up themed context and logging.")

        // Get the base application context provided by Robolectric.
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        // Wrap the base context with our app's theme.
        themedContext = ContextThemeWrapper(applicationContext, R.style.Theme_MTGLifeTracker)
        Logger.unit("ThemedRobolectricTest: Themed context created.")

        // Configure Timber for this test run.
        // First, clear any logging trees that might be lingering from previous test runs.
        Timber.uprootAll()
        // Then, "plant" our custom PrintlnTree. From this point on, all Timber logs
        // in this test will be routed to the console via println.
        Timber.plant(PrintlnTree())
        Logger.unit("ThemedRobolectricTest: Timber configured with PrintlnTree.")
    }
}