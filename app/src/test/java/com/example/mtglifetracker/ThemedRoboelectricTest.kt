package com.example.mtglifetracker

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * An abstract base class for Robolectric tests that require the app's theme.
 * Any test class extending this will automatically have a 'themedContext'
 * available with Theme.MTGLifeTracker applied and logging configured.
 */
@RunWith(RobolectricTestRunner::class)
abstract class ThemedRobolectricTest {

    // A context with the app theme that can be used by any subclass.
    protected lateinit var themedContext: Context

    // Custom tree for unit tests
    private class PrintlnTree : Timber.Tree() {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val formattedTime = LocalDateTime.now().format(formatter)
            println("$formattedTime [${tag ?: "NO_TAG"}] $message")
            t?.printStackTrace()
        }
    }

    @Before
    fun setupThemeAndLogging() {
        // Get the base application context
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        // Apply the app's main theme to create a themed context for the test
        themedContext = ContextThemeWrapper(applicationContext, R.style.Theme_MTGLifeTracker)

        // Plant our custom tree that uses println for unit tests
        // This ensures Timber logs appear in the test execution console
        Timber.uprootAll() // Clear any existing trees
        Timber.plant(PrintlnTree())
    }
}