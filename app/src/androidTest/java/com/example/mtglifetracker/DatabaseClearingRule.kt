package com.example.mtglifetracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mtglifetracker.data.AppDatabase
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A custom JUnit TestRule that automatically clears all tables in the AppDatabase
 * after each test finishes. This ensures that every test runs in a clean,
 * isolated environment without being affected by the state left from previous tests.
 */
class DatabaseClearingRule : TestWatcher() {
    override fun finished(description: Description) {
        super.finished(description)
        val context: Context = ApplicationProvider.getApplicationContext()
        val db = AppDatabase.getDatabase(context)
        db.clearAllTables()
        // Optional: Close the database if necessary, though clearing tables is usually sufficient.
        // db.close()
    }
}