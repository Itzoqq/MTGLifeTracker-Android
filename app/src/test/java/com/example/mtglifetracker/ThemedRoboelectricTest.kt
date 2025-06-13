package com.example.mtglifetracker

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * An abstract base class for Robolectric tests that require the app's theme.
 * Any test class extending this will automatically have a 'themedContext'
 * available with Theme.MTGLifeTracker applied.
 */
@RunWith(RobolectricTestRunner::class)
abstract class ThemedRobolectricTest {

    // A context with the app theme that can be used by any subclass.
    protected lateinit var themedContext: Context

    @Before
    fun setupTheme() {
        // Get the base application context
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()

        // Apply the app's main theme to create a themed context for the test
        themedContext = ContextThemeWrapper(applicationContext, R.style.Theme_MTGLifeTracker)
    }
}