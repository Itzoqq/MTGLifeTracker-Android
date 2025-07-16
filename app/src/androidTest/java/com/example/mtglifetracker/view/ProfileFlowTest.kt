package com.example.mtglifetracker.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.BaseUITest
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An instrumented UI test class for verifying the end-to-end user flow of profile management.
 *
 * This class simulates user interactions with the profile management dialogs to test scenarios
 * such as creating, editing, and deleting profiles, as well as input validation and error handling.
 * It inherits from [BaseUITest] for standard test setup and teardown.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ProfileFlowTest : BaseUITest() {

    /**
     * A helper function to navigate from the main screen to the "Manage Profiles" dialog.
     */
    private fun openManageProfilesDialog() {
        Logger.instrumented("Helper: Navigating to Manage Profiles dialog.")
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withId(R.id.rv_settings_options))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText("Manage Profiles"), click()
            ))
    }

    /**
     * Tests the complete lifecycle of a profile: creation, verification, editing, and deletion.
     */
    @Test
    fun fullProfileFlow_createEditDelete() {
        Logger.instrumented("TEST_START: fullProfileFlow_createEditDelete")
        openManageProfilesDialog()

        // 1. Check initial empty state
        Logger.instrumented("Assert: Verifying initial empty state.")
        onView(withId(R.id.tv_empty_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // 2. Create a new profile
        Logger.instrumented("Act: Creating new profile 'TestProfile'.")
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("TestProfile"))
        onView(withText("Save")).perform(click())

        // 3. Verify profile creation
        Logger.instrumented("Assert: Verifying profile was created and is visible.")
        onView(withText("TestProfile")).check(matches(isDisplayed()))

        // 4. Test Edit Flow
        Logger.instrumented("Act: Editing the new profile.")
        onView(withText("TestProfile")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withText(R.string.title_edit_profile)).check(matches(isDisplayed()))
        onView(withText("Save")).perform(click())

        // 5. Test Delete Flow
        Logger.instrumented("Act: Deleting the profile.")
        onView(withText("TestProfile")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'TestProfile'?")).check(matches(isDisplayed()))
        onView(withText("DELETE")).perform(click())

        // 6. Verify final empty state
        Logger.instrumented("Assert: Verifying final empty state.")
        onView(withId(R.id.tv_empty_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        Logger.instrumented("TEST_PASS: fullProfileFlow_createEditDelete")
    }

    /**
     * Tests that attempting to create a profile with a nickname shorter than 3 characters fails.
     */
    @Test
    fun createProfile_shortNickname_showsError() {
        Logger.instrumented("TEST_START: createProfile_shortNickname_showsError")
        // Arrange
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        // Act
        Logger.instrumented("Act: Entering a short nickname 'AB' and clicking Save.")
        onView(withId(R.id.et_nickname)).perform(replaceText("AB"))
        onView(withText("Save")).perform(click())

        // Assert
        Logger.instrumented("Assert: Verifying Create Profile dialog is still displayed.")
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: createProfile_shortNickname_showsError")
    }

    /**
     * Tests that attempting to create a profile with a nickname longer than 14 characters fails.
     */
    @Test
    fun createProfile_tooLongNickname_showsError() {
        Logger.instrumented("TEST_START: createProfile_tooLongNickname_showsError")
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        Logger.instrumented("Act: Entering a long nickname and clicking Save.")
        onView(withId(R.id.et_nickname)).perform(replaceText("ThisIs15CharsOk"))
        onView(withText("Save")).perform(click())

        Logger.instrumented("Assert: Verifying Create Profile dialog is still displayed.")
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: createProfile_tooLongNickname_showsError")
    }

    /**
     * Tests that attempting to create a profile with non-alphanumeric characters fails.
     */
    @Test
    fun createProfile_nonAlphanumericNickname_showsError() {
        Logger.instrumented("TEST_START: createProfile_nonAlphanumericNickname_showsError")
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        Logger.instrumented("Act: Entering a nickname with symbols and clicking Save.")
        onView(withId(R.id.et_nickname)).perform(replaceText("No!Symbols@"))
        onView(withText("Save")).perform(click())

        Logger.instrumented("Assert: Verifying Create Profile dialog is still displayed.")
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: createProfile_nonAlphanumericNickname_showsError")
    }

    /**
     * Tests that attempting to create a profile with a nickname that already exists fails.
     */
    @Test
    fun createProfile_duplicateNickname_showsError() {
        Logger.instrumented("TEST_START: createProfile_duplicateNickname_showsError")
        // Arrange: Create first profile
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("TestProfile"))
        onView(withText("Save")).perform(click())
        Logger.instrumented("Arrange: First profile created.")

        // Act: Try to create a duplicate
        Logger.instrumented("Act: Attempting to create a duplicate profile.")
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("TestProfile"))
        onView(withText("Save")).perform(click())

        // Assert: Dialog should still be open
        Logger.instrumented("Assert: Verifying Create Profile dialog is still displayed.")
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: createProfile_duplicateNickname_showsError")
    }

    /**
     * Tests that attempting to create a profile with an empty nickname fails.
     */
    @Test
    fun createProfile_emptyNickname_showsError() {
        Logger.instrumented("TEST_START: createProfile_emptyNickname_showsError")
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())

        Logger.instrumented("Act: Clicking Save with an empty nickname.")
        onView(withText("Save")).perform(click())

        Logger.instrumented("Assert: Verifying Create Profile dialog is still displayed.")
        onView(withText("Create Profile")).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: createProfile_emptyNickname_showsError")
    }

    /**
     * Tests that canceling the delete confirmation dialog correctly leaves the profile intact.
     */
    @Test
    fun deleteProfile_cancelConfirmation_profileRemains() {
        Logger.instrumented("TEST_START: deleteProfile_cancelConfirmation_profileRemains")
        // Arrange: Create a profile
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("DoNotDeleteMe"))
        onView(withText("Save")).perform(click())
        Logger.instrumented("Arrange: Profile created.")

        // Act: Start delete process but cancel it
        Logger.instrumented("Act: Starting delete process and canceling at confirmation.")
        onView(withText("DoNotDeleteMe")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'DoNotDeleteMe'?")).check(matches(isDisplayed()))
        onView(withText("Cancel")).perform(click())

        // Assert: Profile still exists
        Logger.instrumented("Assert: Verifying profile still exists.")
        openManageProfilesDialog()
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))
        Logger.instrumented("TEST_PASS: deleteProfile_cancelConfirmation_profileRemains")
    }

    /**
     * Tests that editing a profile to add or change a color is successful.
     */
    @Test
    fun editProfile_changeColor_isSuccessful() {
        Logger.instrumented("TEST_START: editProfile_changeColor_isSuccessful")
        // Arrange: Create a profile without a color
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("EditMyColor"))
        onView(withText("Save")).perform(click())
        // Verify the color dot is initially invisible
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        Logger.instrumented("Arrange: Profile created, color dot is invisible.")

        // Act: Edit the profile to add a color
        Logger.instrumented("Act: Editing profile to add a color.")
        onView(withText("EditMyColor")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withId(R.id.rv_colors)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
        )
        onView(withText("Save")).perform(click())

        // Assert: Color dot is now visible
        Logger.instrumented("Assert: Verifying color dot is now visible.")
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        Logger.instrumented("TEST_PASS: editProfile_changeColor_isSuccessful")
    }

    /**
     * Tests that when editing a profile, its data is correctly pre-filled and the nickname field is disabled.
     */
    @Test
    fun editProfile_mode_populates_data_and_disables_nickname() {
        Logger.instrumented("TEST_START: editProfile_mode_populates_data_and_disables_nickname")
        // Arrange: Create a profile first
        val profileNickname = "TestProfile"
        openManageProfilesDialog()
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText(profileNickname))
        onView(withText("Save")).perform(click())
        Logger.instrumented("Arrange: Profile created.")

        // Act: Open the profile for editing.
        Logger.instrumented("Act: Opening the profile in edit mode.")
        onView(withText(profileNickname)).perform(longClick())
        onView(withText("Edit")).perform(click())

        // Assert
        Logger.instrumented("Assert: Verifying dialog title, pre-filled nickname, and that nickname field is disabled.")
        onView(withText(R.string.title_edit_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.et_nickname)).check(matches(withText(profileNickname)))
        onView(withId(R.id.et_nickname)).check(matches(not(isEnabled())))
        Logger.instrumented("TEST_PASS: editProfile_mode_populates_data_and_disables_nickname")
    }
}