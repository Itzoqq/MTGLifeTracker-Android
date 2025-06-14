package com.example.mtglifetracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.model.Profile
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

@RunWith(AndroidJUnit4::class)
class ProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var profileDao: ProfileDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        profileDao = db.profileDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetProfile() = runTest {
        val profile = Profile(nickname = "Test", color = "#FF0000")
        profileDao.insert(profile)
        val allProfiles = profileDao.getAll().first()
        assertEquals("Test", allProfiles[0].nickname)
    }

    @Test
    @Throws(Exception::class)
    fun getProfileByNickname_isCaseInsensitive() = runTest {
        val profile = Profile(nickname = "Nickname", color = null)
        profileDao.insert(profile)
        val foundProfile = profileDao.getProfileByNickname("nickname")
        assertNotNull(foundProfile)
        assertEquals("Nickname", foundProfile?.nickname)
    }

    @Test
    @Throws(Exception::class)
    fun updateProfile_reflectsChanges() = runTest {
        val profile = Profile(id = 1, nickname = "Original", color = null)
        profileDao.insert(profile)

        val updatedProfile = Profile(id = 1, nickname = "Updated", color = "#00FF00")
        profileDao.update(updatedProfile)

        val retrievedProfile = profileDao.getById(1)
        assertEquals("Updated", retrievedProfile?.nickname)
        assertEquals("#00FF00", retrievedProfile?.color)
    }

    @Test
    @Throws(Exception::class)
    fun deleteById_removesProfile() = runTest {
        val profile = Profile(id = 1, nickname = "ToDelete", color = null)
        profileDao.insert(profile)
        assertNotNull(profileDao.getById(1))

        profileDao.deleteById(1)
        assertNull(profileDao.getById(1))
    }
}