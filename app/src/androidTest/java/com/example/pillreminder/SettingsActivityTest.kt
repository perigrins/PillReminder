package com.example.pillreminder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Instrumented UI tests for [SettingsScreen]
 *
 * The tests use Mockito to simulate FirebaseAuth user states
 */
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    /**
     * Compose testing rule that provides access to the activity context and Compose testing APIs
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser

    /**
     * Sets up a mock FirebaseAuth with a logged-in FirebaseUser before each test
     */
    @Before
    fun setup() {
        mockAuth = mock(FirebaseAuth::class.java)
        mockUser = mock(FirebaseUser::class.java)

        `when`(mockUser.uid).thenReturn("mocked-user-id")
        `when`(mockAuth.currentUser).thenReturn(mockUser)
    }

    /**
     * Verifies that the logged-in UI is displayed when a FirebaseUser is present
     */
    @Test
    fun settingsScreen_ShowsLoggedInUI() {
        composeTestRule.setContent {
            SettingsScreen(fakeAuth = mockAuth, isInTestMode = true)
        }

        composeTestRule.onNodeWithTag("numberOfPillsHeaderText").assertIsDisplayed()
    }

    /**
    * Verifies that the logged-out UI is displayed when FirebaseAuth has no current user
    */
    @Test
    fun settingsScreen_ShowsLoggedOutUI_WhenNoUser() {
        val authWithNoUser = mock(FirebaseAuth::class.java)
        `when`(authWithNoUser.currentUser).thenReturn(null)

        composeTestRule.setContent {
            SettingsScreen(fakeAuth = authWithNoUser, isInTestMode = true)
        }

        composeTestRule.onNodeWithText("Log in or register to unlock all features!")
            .assertIsDisplayed()
    }

    /**
     * Verifies that entering an invalid date shows an error message
     */
    @Test
    fun settingsScreen_InvalidFirstPillDate_ShowsError() {
        composeTestRule.setContent {
            SettingsScreen(fakeAuth = mockAuth, isInTestMode = true)
        }

        composeTestRule.onNodeWithTag("firstPillDateInput")
            .performTextInput("13-08-2025") // wrong date format

        composeTestRule.onNodeWithTag("confirmFirstPillDateButton").performClick()
        composeTestRule.onNodeWithTag("firstPillDateError")
            .assertIsDisplayed()
            .assertTextEquals("Invalid date format")

        //composeTestRule.onNodeWithText("Invalid date format").assertIsDisplayed()
    }

    /**
     * Verifies that entering a valid date proceeds without error
     */
    @Test
    fun settingsScreen_ValidFirstPillDate_SavesSuccessfully() {
        composeTestRule.setContent {
            SettingsScreen(fakeAuth = mockAuth, isInTestMode = true)
        }

        composeTestRule.onNodeWithTag("firstPillDateInput")
            .performTextInput("2025-06-20")
        composeTestRule.onNodeWithTag("confirmFirstPillDateButton").performClick()
    }

    /**
     * Verifies that an incorrect pill count input triggers validation
     */
    @Test
    fun settingsScreen_InvalidPillCount_ShowsValidationMessage() {
        composeTestRule.setContent {
            SettingsScreen(fakeAuth = mockAuth, isInTestMode = true)
        }

        composeTestRule.onNodeWithTag("pillCountInput").performTextInput("0")
        composeTestRule.onNodeWithTag("confirmPillTypeButton").performClick()
        composeTestRule.onNodeWithTag("pillCountError").assertIsDisplayed()
    }

    /**
     * Verifies that toggling reminder switch changes its state
     */
    @Test
    fun settingsScreen_ToggleRemindersSwitch_ChangesState() {
        composeTestRule.setContent {
            SettingsScreen(fakeAuth = mockAuth, isInTestMode = true)
        }
        composeTestRule.waitForIdle()

        val switchNode = composeTestRule.onNodeWithTag("reminderSwitch")

        switchNode.assertIsOff()
        switchNode.performClick()
        switchNode.assertIsOn()
    }
}
