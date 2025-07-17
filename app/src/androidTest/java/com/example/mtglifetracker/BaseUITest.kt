package com.example.mtglifetracker

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.mtglifetracker.data.AppDatabase
import com.example.mtglifetracker.util.Logger
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
 * and idling resources. By centralizing this logic, it ensures that every UI test
 * starts from a known, clean state and that cleanup is performed consistently, which is
 * critical for preventing flaky tests and ensuring test isolation.
 *
 * It automatically manages:
 * 1.  **Hilt Rule:** Sets up Hilt for dependency injection in tests.
 * 2.  **Animation Disabling:** Disables device animations for test stability.
 * 3.  **Activity Launching:** Launches the [MainActivity] before each test.
 * 4.  **Idling Resource:** Registers an [SingletonIdlingResource] to make Espresso wait for background tasks.
 * 5.  **Database Cleanup:** Injects the database instance and clears all tables after each test.
 */
abstract class BaseUITest {

    // (Order 0) The Hilt rule must run first to set up the DI component for the test.
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // (Order 1) The rule to disable animations should run early to prevent timing-related test failures.
    @get:Rule(order = 1)
    val disableAnimationsRule = DisableAnimationsRule()

    // (Order 3) This rule launches the MainActivity after the other setup rules have run.
    @get:Rule(order = 3)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Injects the test [AppDatabase] instance provided by Hilt's [com.example.mtglifetracker.di.TestAppModule].
     * This allows tests to directly interact with the database for setup or verification.
     */
    @Inject
    lateinit var db: AppDatabase

    /**
     * Sets up the test environment before each test method is executed.
     * This function handles Hilt dependency injection and registers the global idling resource.
     */
    @Before
    open fun setUp() {
        Logger.instrumented("BaseUITest: setUp started.")
        // Injects dependencies (like the 'db' instance) into the test class.
        hiltRule.inject()
        Logger.instrumented("BaseUITest: Hilt dependencies injected.")
        // Registers the counting idling resource with Espresso. From this point on, Espresso will
        // wait for the resource's counter to be zero before performing any view actions.
        IdlingRegistry.getInstance().register(SingletonIdlingResource.countingIdlingResource)
        Logger.instrumented("BaseUITest: IdlingResource registered. Setup complete.")
    }

    /**
     * Tears down the test environment after each test method has finished executing.
     * This function handles cleanup to ensure that each test is isolated and does not
     * affect subsequent tests.
     */
    @After
    open fun tearDown() {
        Logger.instrumented("BaseUITest: tearDown started.")
        // Unregister the idling resource to prevent memory leaks.
        IdlingRegistry.getInstance().unregister(SingletonIdlingResource.countingIdlingResource)
        Logger.instrumented("BaseUITest: IdlingResource unregistered.")

        // Use runBlocking to execute the suspending database cleanup operation synchronously
        // within the @After block.
        runBlocking {
            // This is a critical step for test isolation. It clears all data from all tables
            // in the in-memory database, ensuring the next test starts with a clean slate.
            Logger.instrumented("BaseUITest: Clearing all database tables.")
            db.clearAllTables()
            db.close()
            Logger.instrumented("BaseUITest: Database tables cleared.")
        }

        // Close the activity scenario to free up resources.
        activityRule.scenario.close()
        Logger.instrumented("BaseUITest: ActivityScenario closed. Teardown complete.")
    }
}