package com.example.mtglifetracker

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * A custom [AndroidJUnitRunner] required for Hilt to work in instrumented tests.
 *
 * This class is referenced in the `app/build.gradle.kts` file (`testInstrumentationRunner`).
 * Its purpose is to override the default application creation process during a test run.
 * Instead of using the production `MTGLifeTrackerApp`, it instructs the test framework
 * to use Hilt's special [HiltTestApplication]. This is the hook that allows Hilt to
 * provide test-specific dependencies (like an in-memory database from [com.example.mtglifetracker.di.TestAppModule])
 * to your tests and the application components being tested.
 *
 * This is standard Hilt boilerplate and generally does not need to be modified.
 */
@Suppress("unused")
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        // Force the test runner to use HiltTestApplication.
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}