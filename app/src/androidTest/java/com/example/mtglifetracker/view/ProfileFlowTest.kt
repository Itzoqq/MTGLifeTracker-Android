package com.example.mtglifetracker.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mtglifetracker.DatabaseClearingRule
import com.example.mtglifetracker.DisableAnimationsRule
import com.example.mtglifetracker.MainActivity
import com.example.mtglifetracker.R
import com.example.mtglifetracker.SingletonIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ProfileFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule(order = 2)
    val clearDatabaseRule = DatabaseClearingRule()

    @get:Rule(order = 3)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(SingletonIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(SingletonIdlingResource.countingIdlingResource)
    }

    @Test
    fun fullProfileFlow_createEditDelete() {
        // 1. Open settings -> Manage Profiles
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())

        // 2. Check that the empty state is visible and the list is not
        onView(withId(R.id.tv_empty_profiles))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.rv_profiles))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // 3. Create a new profile
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("Test Profile"))
        onView(withText("Save")).perform(click())

        // 4. Verify the new profile is in the list and the empty state is gone
        onView(withId(R.id.rv_profiles))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.tv_empty_profiles))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withText("Test Profile")).check(matches(isDisplayed()))

        // 5. Test Edit Flow
        onView(withText("Test Profile")).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withText("Edit Profile")).check(matches(isDisplayed()))
        onView(withText("Save")).perform(click())

        // 6. Test Delete Flow
        onView(withText("Test Profile")).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Are you sure you want to delete 'Test Profile'?")).check(matches(isDisplayed()))
        onView(withText("DELETE")).perform(click())

        // 7. FINAL, ROBUST VERIFICATION:
        // Check that the empty state TextView is now displayed and the RecyclerView is GONE.
        onView(withId(R.id.tv_empty_profiles))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.rv_profiles))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun createProfile_shortNickname_showsError() {
        // Open manage profiles
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())

        // Try to create profile with short nickname
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("AB")) // Only 2 characters
        onView(withText("Save")).perform(click())

        // Dialog should still be open (not dismissed due to error)
        onView(withText("Create Profile")).check(matches(isDisplayed()))

        // Cancel the dialog
        onView(withText("Cancel")).perform(click())

        // Should still show empty state
        onView(withText("No profiles created yet.")).check(matches(isDisplayed()))
    }

    @Test
    fun createProfile_duplicateNickname_showsError() {
        // Create first profile
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("Test Profile"))
        onView(withText("Save")).perform(click())

        // Try to create duplicate
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("Test Profile"))
        onView(withText("Save")).perform(click())

        // Dialog should still be open (not dismissed due to error)
        onView(withText("Create Profile")).check(matches(isDisplayed()))

        // Cancel the dialog
        onView(withText("Cancel")).perform(click())

        // Should only have one profile
        onView(withText("Test Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun createProfile_emptyNickname_showsError() {
        // 1. Open manage profiles dialog
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())

        // 2. Click the FAB to open the create profile dialog
        onView(withId(R.id.fab_add_profile)).perform(click())

        // 3. Leave the nickname empty and click "Save"
        onView(withText("Save")).perform(click())

        // 4. Verify the "Create Profile" dialog is still visible,
        //    which means the save was blocked due to the error.
        onView(withText("Create Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun deleteProfile_cancelConfirmation_profileRemains() {
        // 1. Create a profile to work with
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("DoNotDeleteMe"))
        onView(withText("Save")).perform(click())

        // 2. Verify it exists
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))

        // 3. Start the delete process
        onView(withText("DoNotDeleteMe")).perform(longClick())
        onView(withText("Delete")).perform(click())

        // 4. On the confirmation dialog, click "Cancel" (the negative button)
        onView(withText("Are you sure you want to delete 'DoNotDeleteMe'?"))
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button2)).perform(click())

        // 5. Verify the profile still exists in the list
        onView(withText("Manage Profiles")).check(matches(isDisplayed()))
        onView(withText("DoNotDeleteMe")).check(matches(isDisplayed()))
    }

    @Test
    fun editProfile_changeColor_isSuccessful() {
        // 1. Create a profile with no color
        onView(withId(R.id.settingsIcon)).perform(click())
        onView(withText("Manage Profiles")).perform(click())
        onView(withId(R.id.fab_add_profile)).perform(click())
        onView(withId(R.id.et_nickname)).perform(replaceText("EditMyColor"))
        onView(withText("Save")).perform(click())

        // 2. Verify the color dot view is not visible initially
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // 3. Open the Edit dialog
        onView(withText("EditMyColor")).perform(longClick())
        onView(withText("Edit")).perform(click())

        // 4. In the Edit dialog, click the first color swatch
        // Note: We find the grid and then click on a child view within it.
        onView(allOf(isDescendantOfA(withId(R.id.grid_colors)), withParentIndex(0)))
            .perform(click())
        onView(withText("Save")).perform(click())

        // 5. Verify the profile is back in the list and its color dot is now visible
        onView(withText("EditMyColor")).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.view_profile_color), hasSibling(withText("EditMyColor"))))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}