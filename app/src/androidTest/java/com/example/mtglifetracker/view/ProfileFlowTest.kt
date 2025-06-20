package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ProfileFlowTest : BaseUITest() {

    @Test
    fun fullProfileFlow_createEditDelete() {
        // 1. Open settings -> Manage Profiles
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())

        // 2. Check initial empty state
        onView(withId(R.id.tv_empty_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // 3. Create a new profile
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed())) // Assert dialog is open
        onView(withId(R.id.et_nickname)).perform(replaceText("Test Profile"))
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // 4. Verify profile creation
        onView(withText("Test Profile")).check(matches(isDisplayed()))

        // 5. Test Edit Flow
        onView(withText("Test Profile")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withText("Edit Profile")).check(matches(isDisplayed())) // Assert dialog is open
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // 6. Test Delete Flow
        onView(withText("Test Profile")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'Test Profile'?")).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click()) // Click "DELETE"

        // 7. Verify final empty state
        onView(withId(R.id.tv_empty_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun createProfile_shortNickname_showsError() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())

        onView(withText("Create Profile")).check(matches(isDisplayed())) // Assert dialog is open
        onView(withId(R.id.et_nickname)).perform(replaceText("AB")) // Too short
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // Assert: Dialog should still be open
        onView(withText("Create Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun createProfile_duplicateNickname_showsError() {
        // Arrange: Create first profile
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("Test Profile"))
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // Act: Try to create duplicate
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("Test Profile"))
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // Assert: Dialog should still be open
        onView(withText("Create Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun createProfile_emptyNickname_showsError() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())

        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // Assert Dialog is still displayed
        onView(withText("Create Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun deleteProfile_cancelConfirmation_profileRemains() {
        // 1. Arrange: Create a profile
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("DoNotDeleteMe"))
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))

        // 2. Act: Start delete process but cancel it
        onView(withText("DoNotDeleteMe")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'DoNotDeleteMe'?")).check(matches(isDisplayed()))
        onView(withId(android.R.id.button2)).perform(click()) // Click "Cancel"

        // 3. Assert: Profile still exists
        onView(withText("Manage Profiles")).check(matches(isDisplayed()))
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))
    }

    @Test
    fun editProfile_changeColor_isSuccessful() {
        // 1. Arrange: Create a profile
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("EditMyColor"))
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // 2. Act: Edit the profile to add a color
        onView(withText("EditMyColor")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withText("Edit Profile")).check(matches(isDisplayed()))
        onView(allOf(isDescendantOfA(withId(R.id.grid_colors)), withParentIndex(0))).perform(click())
        onView(withId(android.R.id.button1)).perform(click()) // Click "Save"

        // 3. Assert: Color dot is now visible
        onView(withText("EditMyColor")).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}