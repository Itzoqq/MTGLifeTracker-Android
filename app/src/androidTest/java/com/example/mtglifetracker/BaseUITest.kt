package com.example.mtglifetracker

import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.example.mtglifetracker.data.AppDatabase
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.Locale
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
 * 6.  **ANR Handling:** Sets a custom failure handler to dismiss ANR dialogs.
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
    private var anrCount = 0

    /**
     * Sets up the test environment before each test method is executed.
     * This function handles Hilt dependency injection and registers the global idling resource.
     */
    @Before
    open fun setUp() {
        Logger.instrumented("BaseUITest: setUp started.")
        hiltRule.inject()
        Logger.instrumented("BaseUITest: Hilt dependencies injected.")
        IdlingRegistry.getInstance().register(SingletonIdlingResource.countingIdlingResource)
        Logger.instrumented("BaseUITest: IdlingResource registered.")

        // **THE FIX**: Set up the custom failure handler to catch ANR dialogs.
        setupFailureHandler()
        Logger.instrumented("BaseUITest: Custom failure handler set up. Setup complete.")
    }

    /**
     * Tears down the test environment after each test method has finished executing.
     * This function handles cleanup to ensure that each test is isolated and does not
     * affect subsequent tests.
     */
    @After
    open fun tearDown() {
        Logger.instrumented("BaseUITest: tearDown started.")
        IdlingRegistry.getInstance().unregister(SingletonIdlingResource.countingIdlingResource)
        Logger.instrumented("BaseUITest: IdlingResource unregistered.")

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // **THE FIX**: Use the non-deprecated way to get the default SharedPreferences and clear it.
        val sharedPrefsName = "${context.packageName}_preferences"
        val sharedPreferences = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        Logger.instrumented("BaseUITest: SharedPreferences cleared.")


        runBlocking {
            Logger.instrumented("BaseUITest: Clearing all database tables.")
            db.clearAllTables()
            db.close()
            Logger.instrumented("BaseUITest: Database tables cleared.")
        }

        activityRule.scenario.close()
        Logger.instrumented("BaseUITest: ActivityScenario closed. Teardown complete.")
    }

    /**
     * Sets a custom failure handler for Espresso to automatically dismiss ANR dialogs.
     */
    private fun setupFailureHandler() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val rootCauseMsg = String.format(
            Locale.ROOT,
            "Waited for the root of the view hierarchy to have window focus and not request layout for 10 seconds. If you specified a non default root matcher, it may be picking a root that never takes focus. Root:"
        )

        Espresso.setFailureHandler { error, viewMatcher ->
            // Check if the error is the specific ANR-related exception and we haven't tried to handle it too many times.
            if (error.message?.contains(rootCauseMsg) == true && anrCount < 2) {
                anrCount++
                Logger.instrumented("BaseUITest: ANR detected. Attempting to dismiss dialog (Attempt #$anrCount).")
                handleAnrDialog()
                // IMPORTANT: Re-throw the error to make the current test fail, but allow subsequent tests to run.
                throw error
            } else {
                // For all other errors, use the default Espresso failure handler.
                DefaultFailureHandler(context).handle(error, viewMatcher)
            }
        }
    }

    /**
     * Uses UiAutomator to find and click the "Wait" button on a system ANR dialog.
     */
    private fun handleAnrDialog() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // The text on the button might vary by Android version/locale, "Wait" is common for English.
        val waitButton = device.findObject(UiSelector().textContains("Wait"))
        if (waitButton.exists()) {
            try {
                Logger.instrumented("BaseUITest: Found 'Wait' button on ANR dialog. Clicking it.")
                waitButton.click()
            } catch (e: Exception) {
                Logger.e(e, "BaseUITest: Error clicking ANR dialog wait button.")
            }
        } else {
            Logger.w("BaseUITest: ANR dialog detected, but 'Wait' button was not found.")
        }
    }
}