package com.example.mtglifetracker

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.After
import org.junit.Before
import org.junit.Rule

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
 * 3.  Clears the database before each test to ensure state isolation.
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

    // (Order 2) Rule to clear the database before each test, ensuring a fresh start.
    @get:Rule(order = 2)
    val clearDatabaseRule = DatabaseClearingRule()

    // (Order 3) Rule to launch the MainActivity before each test.
    @get:Rule(order = 3)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * This method is run before each test. It registers the global idling resource
     * to ensure that Espresso waits for any asynchronous operations to complete
     * before proceeding with test actions.
     */
    @Before
    open fun setUp() {
        IdlingRegistry.getInstance().register(SingletonIdlingResource.countingIdlingResource)
    }

    /**
     * This method is run after each test. It performs two critical cleanup actions:
     * 1.  Unregisters the idling resource.
     * 2.  Performs a back press to close any dialogs or screens that may have been
     * left open, especially if a test failed midway. This helps reset the
     * app to a baseline state for the next test.
     */
    @After
    open fun tearDown() {
        IdlingRegistry.getInstance().unregister(SingletonIdlingResource.countingIdlingResource)

        // As a final cleanup step, unconditionally press the back button. This helps
        // close any lingering dialogs that a failed test might have left open.
        try {
            Espresso.pressBackUnconditionally()
        } catch (_: Exception) {
            // Ignore exceptions, as the activity might already be closed.
        }
    }
}