package com.example.mtglifetracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.model.Profile
import com.example.mtglifetracker.util.Logger
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for the [ProfileDao].
 *
 * This class verifies all database operations related to the `profiles` table,
 * ensuring that creating, reading, updating, and deleting profiles work as expected.
 * It uses an in-memory database for isolation and speed.
 */
@RunWith(AndroidJUnit4::class)
class ProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var profileDao: ProfileDao

    /**
     * Creates a fresh in-memory database before each test.
     */
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        profileDao = db.profileDao()
        Logger.instrumented("ProfileDaoTest: In-memory database created.")
    }

    /**
     * Closes the database connection after each test to release resources.
     */
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
        Logger.instrumented("ProfileDaoTest: In-memory database closed.")
    }

    /**
     * Tests that inserting a profile and then getting all profiles works correctly.
     */
    @Test
    @Throws(Exception::class)
    fun insertAndGetProfile() = runTest {
        Logger.instrumented("TEST_START: insertAndGetProfile")
        // Arrange
        val profile = Profile(nickname = "Test", color = "#FF0000")
        Logger.instrumented("Arrange: Created profile 'Test'.")

        // Act
        Logger.instrumented("Act: Inserting profile.")
        profileDao.insert(profile)
        val allProfiles = profileDao.getAll().first()
        Logger.instrumented("Act: Retrieved ${allProfiles.size} profiles.")

        // Assert
        Logger.instrumented("Assert: Verifying retrieved profile.")
        assertEquals("Test", allProfiles[0].nickname)
        Logger.instrumented("TEST_PASS: insertAndGetProfile")
    }

    /**
     * Tests that the nickname search is case-insensitive as defined by the query.
     */
    @Test
    @Throws(Exception::class)
    fun getProfileByNickname_isCaseInsensitive() = runTest {
        Logger.instrumented("TEST_START: getProfileByNickname_isCaseInsensitive")
        // Arrange
        val profile = Profile(nickname = "Nickname", color = null)
        profileDao.insert(profile)
        Logger.instrumented("Arrange: Inserted profile with nickname 'Nickname'.")

        // Act
        Logger.instrumented("Act: Searching for profile with lowercase 'nickname'.")
        val foundProfile = profileDao.getProfileByNickname("nickname")

        // Assert
        Logger.instrumented("Assert: Verifying that the profile was found.")
        assertNotNull(foundProfile)
        assertEquals("Nickname", foundProfile?.nickname)
        Logger.instrumented("TEST_PASS: getProfileByNickname_isCaseInsensitive")
    }

    /**
     * Tests that updating a profile correctly persists the changes.
     */
    @Test
    @Throws(Exception::class)
    fun updateProfile_reflectsChanges() = runTest {
        Logger.instrumented("TEST_START: updateProfile_reflectsChanges")
        // Arrange
        val profile = Profile(id = 1, nickname = "Original", color = null)
        profileDao.insert(profile)
        Logger.instrumented("Arrange: Inserted profile 'Original'.")
        val updatedProfile = Profile(id = 1, nickname = "Updated", color = "#00FF00")

        // Act
        Logger.instrumented("Act: Updating profile to 'Updated' with a new color.")
        profileDao.update(updatedProfile)
        val retrievedProfile = profileDao.getById(1)

        // Assert
        Logger.instrumented("Assert: Verifying profile data was updated.")
        assertEquals("Updated", retrievedProfile?.nickname)
        assertEquals("#00FF00", retrievedProfile?.color)
        Logger.instrumented("TEST_PASS: updateProfile_reflectsChanges")
    }

    /**
     * Tests that deleting a profile by its ID removes it from the database.
     */
    @Test
    @Throws(Exception::class)
    fun deleteById_removesProfile() = runTest {
        Logger.instrumented("TEST_START: deleteById_removesProfile")
        // Arrange
        val profile = Profile(id = 1, nickname = "ToDelete", color = null)
        profileDao.insert(profile)
        assertNotNull(profileDao.getById(1))
        Logger.instrumented("Arrange: Inserted profile 'ToDelete'.")

        // Act
        Logger.instrumented("Act: Deleting profile with ID 1.")
        profileDao.deleteById(1)
        val retrievedProfile = profileDao.getById(1)

        // Assert
        Logger.instrumented("Assert: Verifying profile is null after deletion.")
        assertNull(retrievedProfile)
        Logger.instrumented("TEST_PASS: deleteById_removesProfile")
    }
}