package com.example.mtglifetracker

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.mtglifetracker.data.GameRepository
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

    // MODIFICATION: Inject the repository to handle database cleanup.
    @Inject
    lateinit var repository: GameRepository

    /**
     * This method is run before each test. It injects dependencies via Hilt
     * and registers the global idling resource.
     */
    @Before
    open fun setUp() {
        // MODIFICATION: This call is essential to populate the @Inject fields like the repository.
        hiltRule.inject()
        IdlingRegistry.getInstance().register(SingletonIdlingResource.countingIdlingResource)
    }

    /**
     * This method is run after each test. It performs critical cleanup actions:
     * 1. Unregisters the idling resource.
     * 2. Clears all tables in the Hilt-managed database to ensure test isolation.
     * 3. Performs a back press to reset the UI state for the next test.
     */
    @After
    open fun tearDown() {
        IdlingRegistry.getInstance().unregister(SingletonIdlingResource.countingIdlingResource)

        // MODIFICATION: Use runBlocking to call the suspend function from this non-coroutine context.
        // This clears the single, Hilt-managed database instance, ensuring test isolation.
        runBlocking {
            repository.resetAllGames()
        }

        // As a final cleanup step, unconditionally press the back button. This helps
        // close any lingering dialogs that a failed test might have left open.
        try {
            Espresso.pressBackUnconditionally()
        } catch (_: Exception) {
            // Ignore exceptions, as the activity might already be closed.
        }
    }
}