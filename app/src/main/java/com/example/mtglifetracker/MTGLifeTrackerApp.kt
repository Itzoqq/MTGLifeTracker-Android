package com.example.mtglifetracker

import android.app.Application
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * The custom [Application] class for the MTG Life Tracker app.
 *
 * This class serves as the main entry point for the application process. Its primary
 * responsibilities are to initialize application-level components and singletons that
 * need to exist for the entire lifecycle of the app.
 *
 * The `@HiltAndroidApp` annotation is crucial. It triggers Hilt's code generation,
 * which sets up the dependency injection container for the entire application.
 *
 * This class is also responsible for initializing the Timber logging library for the
 * production/debug builds of the app.
 */
@HiltAndroidApp
class MTGLifeTrackerApp : Application() {

    /**
     * Called when the application is starting, before any other components have been created.
     * This is the ideal place to perform one-time initializations.
     */
    override fun onCreate() {
        super.onCreate()
        // This is one of the first lines of your own code that will run.
        Logger.i("MTGLifeTrackerApp: onCreate - Application process is starting.")

        // "Plant" a debug tree for Timber. In a debug build, this will automatically
        // use Android's Logcat and add useful information like the class name and line
        // number to each log message. In a release build, you might plant a different
        // tree that sends crash reports instead.
        Timber.plant(Timber.DebugTree())
        Logger.i("MTGLifeTrackerApp: Timber logging has been initialized for the application.")
    }
}