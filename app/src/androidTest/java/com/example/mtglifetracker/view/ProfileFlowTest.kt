package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
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

    private fun openManageProfilesDialog() {
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Manage Profiles"), click()
            ))
    }

    @Test
    fun fullProfileFlow_createEditDelete() {
        openManageProfilesDialog()

        // 2. Check initial empty state
        onView(withId(R.id.tv_empty_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // 3. Create a new profile
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("TestProfile"))
        onView(withText("Save")).perform(click())

        // 4. Verify profile creation
        onView(withText("TestProfile")).check(matches(isDisplayed()))

        // 5. Test Edit Flow
        onView(withText("TestProfile")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withText(R.string.title_edit_profile)).check(matches(isDisplayed()))
        onView(withText("Save")).perform(click())

        // 6. Test Delete Flow
        onView(withText("TestProfile")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'TestProfile'?")).check(matches(isDisplayed()))
        onView(withText("DELETE")).perform(click())

        // 7. Verify final empty state
        onView(withId(R.id.tv_empty_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun createProfile_shortNickname_showsError() {
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("AB"))
        onView(withText("Save")).perform(click())

        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
    }

    // --- NEW TEST ---
    @Test
    fun createProfile_tooLongNickname_showsError() {
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        // Attempt to enter a 15-character nickname
        onView(withId(R.id.et_nickname)).perform(replaceText("ThisIs15CharsOk"))
        onView(withText("Save")).perform(click())

        // Assert Dialog is still displayed because the text is too long.
        // Even though the view has maxLength, this tests the logic just in case.
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
    }

    // --- NEW TEST ---
    @Test
    fun createProfile_nonAlphanumericNickname_showsError() {
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("No!Symbols@"))
        onView(withText("Save")).perform(click())

        // Assert Dialog is still displayed
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
    }


    @Test
    fun createProfile_duplicateNickname_showsError() {
        // Arrange: Create first profile
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("TestProfile"))
        onView(withText("Save")).perform(click())

        // Act: Try to create duplicate
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("TestProfile"))
        onView(withText("Save")).perform(click())

        // Assert: Dialog should still be open
        onView(withText("Create Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun createProfile_emptyNickname_showsError() {
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withText("Save")).perform(click())

        // Assert Dialog is still displayed
        onView(withText("Create Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun deleteProfile_cancelConfirmation_profileRemains() {
        // 1. Arrange: Create a profile
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("DoNotDeleteMe"))
        onView(withText("Save")).perform(click())
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))

        // 2. Act: Start delete process but cancel it
        onView(withText("DoNotDeleteMe")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'DoNotDeleteMe'?")).check(matches(isDisplayed()))
        onView(withText("Cancel")).perform(click())

        // 3. Assert: Profile still exists
        onView(withText("Manage Profiles")).check(matches(isDisplayed()))
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))
    }

    @Test
    fun editProfile_changeColor_isSuccessful() {
        // 1. Arrange: Create a profile
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).perform(replaceText("EditMyColor"))
        onView(withText("Save")).perform(click())
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // 2. Act: Edit the profile to add a color
        onView(withText("EditMyColor")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withText("Edit Profile")).check(matches(isDisplayed()))

        onView(withId(R.id.rv_colors)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
        )

        onView(withText("Save")).perform(click())

        // 3. Assert: Color dot is now visible
        onView(withText("EditMyColor")).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}