package com.example.mtglifetracker.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate5To6_withData() {
        // FIX: Removed the unnecessary 'var db =' assignment
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("INSERT INTO game_settings (id, playerCount) VALUES (1, 2)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        val cursor = db.query("SELECT * FROM game_settings")
        cursor.moveToFirst()
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("playerCount")))
        assertEquals(40, cursor.getInt(cursor.getColumnIndexOrThrow("startingLife")))
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6_onEmptyDatabase() {
        helper.createDatabase(TEST_DB, 5).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)
        val cursor = db.query("SELECT * FROM game_settings")
        assertEquals(0, cursor.count)
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_transformsExistingProfiles() {
        // FIX: Removed the unnecessary 'val db =' assignment
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL("INSERT INTO profiles (id, nickname, color) VALUES (1, 'TestUser', '#FF0000')")
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        val cursor = migratedDb.query("SELECT * FROM profiles WHERE id = 1")
        cursor.moveToFirst()
        val isDefaultValue = cursor.getInt(cursor.getColumnIndexOrThrow("isDefault"))
        assertEquals(0, isDefaultValue)
    }

    @Test
    @Throws(IOException::class)
    fun migrate7To8_dropsIsDefaultColumn() {
        val testNickname = "TestUser"
        // FIX: Removed the unnecessary 'val db =' assignment
        helper.createDatabase(TEST_DB, 7).apply {
            execSQL("INSERT INTO profiles (id, nickname, color, isDefault) VALUES (1, '$testNickname', null, 0)")
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 8, true, AppDatabase.MIGRATION_7_8)

        val cursor = migratedDb.query("SELECT * FROM profiles WHERE id = 1")
        cursor.moveToFirst()
        assertEquals(testNickname, cursor.getString(cursor.getColumnIndexOrThrow("nickname")))

        try {
            cursor.getInt(cursor.getColumnIndexOrThrow("isDefault"))
            throw AssertionError("The 'isDefault' column was not dropped.")
        } catch (_: IllegalArgumentException) {
            // This is the expected outcome. The column does not exist. Test passes.
        }
    }
}