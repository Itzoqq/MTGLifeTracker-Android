package com.example.mtglifetracker

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.mtglifetracker.data.AppDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

/**
 * An abstract base class for all UI tests to inherit from.
 *
 * This class handles the boilerplate setup and teardown for essential test rules
 * and idling resources. By centralizing this logic, it ensures that every test
 * starts from a known, clean state and that cleanup is performed consistently,
 * which is critical for preventing flaky tests.
 *
 * It automatically:
 * 1.  Sets up Hilt for dependency injection in tests.
 * 2.  Disables device animations to prevent timing-related test failures.
 * 3.  Injects Hilt dependencies and clears the database via the repository after each test.
 * 4.  Launches the MainActivity.
 * 5.  Registers an IdlingResource to make Espresso wait for background tasks.
 * 6.  Performs cleanup after each test, including pressing the back button to
 * dismiss any lingering dialogs or screens.
 */
abstract class BaseUITest {

    // (Order 0) Hilt Rule for dependency injection.
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // (Order 1) Rule to disable animations for test stability.
    @get:Rule(order = 1)
    val disableAnimationsRule = DisableAnimationsRule()

    // (Order 3) Rule to launch the MainActivity before each test.
    @get:Rule(order = 3)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var db: AppDatabase

    @Before
    open fun setUp() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(SingletonIdlingResource.countingIdlingResource)
    }

    @After
    open fun tearDown() {
        IdlingRegistry.getInstance().unregister(SingletonIdlingResource.countingIdlingResource)

        // --- THIS IS THE FIX ---
        // 1. Use Room's built-in, thread-safe method to clear all tables.
        //    This is safer than calling your own repository methods here.
        runBlocking {
            db.clearAllTables()
        }
        // 2. Now that the database is cleared and no operations are pending,
        //    close the connection to prevent the leak warning.
        db.close()
        // --- END OF FIX ---

        try {
            Espresso.pressBackUnconditionally()
        } catch (_: Exception) {
            // Ignore exceptions
        }
    }
}