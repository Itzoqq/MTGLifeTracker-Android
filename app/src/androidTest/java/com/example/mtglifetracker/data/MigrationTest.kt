package com.example.mtglifetracker.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration-test"

/**
 * Instrumented tests for Room database migrations.
 *
 * This class uses a [MigrationTestHelper] to verify that database schema changes
 * across different versions work as expected and that user data is correctly preserved.
 * Each test simulates a migration from one version to the next.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    // A JUnit Rule that provides a helper for testing Room migrations.
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * Tests the migration from version 5 to 6, which adds the `startingLife` column.
     * This test ensures data in the table is preserved.
     */
    @Test
    @Throws(IOException::class)
    fun migrate5To6_withData() {
        Logger.instrumented("TEST_START: migrate5To6_withData")
        // Arrange: Create a version 5 database and insert some data.
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("INSERT INTO game_settings (id, playerCount) VALUES (1, 2)")
            close()
        }
        Logger.instrumented("Arrange: Created v5 DB with one settings entry.")

        // Act: Run the migration to version 6.
        Logger.instrumented("Act: Running migration from v5 to v6.")
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // Assert: Verify that the old data is still present and the new column has its default value.
        Logger.instrumented("Assert: Querying migrated DB to check data.")
        db.query("SELECT * FROM game_settings").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("playerCount")))
            assertEquals(40, cursor.getInt(cursor.getColumnIndexOrThrow("startingLife")))
        }
        Logger.instrumented("TEST_PASS: migrate5To6_withData")
    }

    /**
     * Tests the migration from version 5 to 6 on an empty database.
     */
    @Test
    @Throws(IOException::class)
    fun migrate5To6_onEmptyDatabase() {
        Logger.instrumented("TEST_START: migrate5To6_onEmptyDatabase")
        // Arrange: Create an empty version 5 database.
        helper.createDatabase(TEST_DB, 5).close()
        Logger.instrumented("Arrange: Created empty v5 DB.")

        // Act: Run the migration to version 6.
        Logger.instrumented("Act: Running migration from v5 to v6.")
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // Assert: Verify the database is still empty.
        Logger.instrumented("Assert: Querying migrated DB to ensure it's still empty.")
        db.query("SELECT * FROM game_settings").use { cursor ->
            assertEquals(0, cursor.count)
        }
        Logger.instrumented("TEST_PASS: migrate5To6_onEmptyDatabase")
    }

    /**
     * Tests the migration from version 6 to 7, which added the (now removed) `isDefault` column.
     * This ensures the migration path is valid.
     */
    @Test
    @Throws(IOException::class)
    fun migrate6To7_transformsExistingProfiles() {
        Logger.instrumented("TEST_START: migrate6To7_transformsExistingProfiles")
        // Arrange
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL("INSERT INTO profiles (id, nickname, color) VALUES (1, 'TestUser', '#FF0000')")
            close()
        }
        Logger.instrumented("Arrange: Created v6 DB with one profile.")

        // Act
        Logger.instrumented("Act: Running migration from v6 to v7.")
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        // Assert
        Logger.instrumented("Assert: Verifying new 'isDefault' column has correct default value.")
        migratedDb.query("SELECT * FROM profiles WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            val isDefaultValue = cursor.getInt(cursor.getColumnIndexOrThrow("isDefault"))
            assertEquals(0, isDefaultValue)
        }
        Logger.instrumented("TEST_PASS: migrate6To7_transformsExistingProfiles")
    }

    /**
     * Tests the migration from version 7 to 8, which removes the `isDefault` column.
     */
    @Test
    @Throws(IOException::class)
    fun migrate7To8_dropsIsDefaultColumn() {
        Logger.instrumented("TEST_START: migrate7To8_dropsIsDefaultColumn")
        // Arrange
        val testNickname = "TestUser"
        helper.createDatabase(TEST_DB, 7).apply {
            execSQL("INSERT INTO profiles (id, nickname, color, isDefault) VALUES (1, '$testNickname', null, 0)")
            close()
        }
        Logger.instrumented("Arrange: Created v7 DB with one profile including 'isDefault' column.")

        // Act
        Logger.instrumented("Act: Running migration from v7 to v8.")
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 8, true, AppDatabase.MIGRATION_7_8)

        // Assert
        Logger.instrumented("Assert: Verifying 'isDefault' column was dropped.")
        migratedDb.query("SELECT * FROM profiles WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals(testNickname, cursor.getString(cursor.getColumnIndexOrThrow("nickname")))

            try {
                // This call should fail if the migration was successful.
                cursor.getInt(cursor.getColumnIndexOrThrow("isDefault"))
                // If it doesn't fail, we force the test to fail.
                throw AssertionError("The 'isDefault' column was not dropped.")
            } catch (_: IllegalArgumentException) {
                // This exception is expected, meaning the column doesn't exist. Test passes.
                Logger.instrumented("Assert: Successfully caught IllegalArgumentException, column was dropped.")
            }
        }
        Logger.instrumented("TEST_PASS: migrate7To8_dropsIsDefaultColumn")
    }
}