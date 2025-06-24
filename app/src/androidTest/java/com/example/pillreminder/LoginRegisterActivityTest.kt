package com.example.pillreminder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

/**
 * Instrumented UI tests for [LoginRegisterActivity]
 *
 * The tests use Mockito to simulate FirebaseAuth user states
 */
@RunWith(AndroidJUnit4::class)
class LoginRegisterActivityTest {
    /**
     * Compose testing rule that provides access to the activity context and Compose testing APIs
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAuth: FirebaseAuth

    /**
     * Sets up a mock FirebaseAuth with a logged-in FirebaseUser before each test
     */
    @Before
    fun setup() {
        mockAuth = Mockito.mock(FirebaseAuth::class.java)
        Mockito.`when`(mockAuth.currentUser).thenReturn(null)
    }

    /**
     * Verifies if the login view is displayed by default
     */
    @Test
    fun authSwitchContent_showsLoginByDefault() {
        composeTestRule.setContent {
            AuthSwitchContent(auth = mockAuth)
        }

        composeTestRule.onNodeWithTag("LoginHeader", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }

    /**
     * Verifies switching to register view
     */
    @Test
    fun authSwitchContent_switchesToRegister() {
        composeTestRule.setContent {
            AuthSwitchContent(auth = mockAuth)
        }

        composeTestRule.onNodeWithTag("LoginHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("RegisterLoginButton", useUnmergedTree = true)
            .performClick()
        composeTestRule.onNodeWithTag("RegisterHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("RegisterLoginButton", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("Already have an account? Login")
    }

    /**
     * Verifies logging out the user
     */
    @Test
    fun logOutScreen_callsOnLogout() {
        var loggedOut = false
        composeTestRule.setContent {
            LogOutScreen(email = "user@example.com", onLogout = { loggedOut = true })
        }

        composeTestRule.onNodeWithText("Log out").performClick()
        TestCase.assertTrue(loggedOut)
    }

    /**
     * Verifies the pattern of an email
     */
    @Test
    fun isValidEmail_validAndInvalidEmails() {
        TestCase.assertTrue(isValidEmail("test@example.com"))
        TestCase.assertFalse(isValidEmail("invalid-email"))
    }


    @Test
    fun loginScreen_inputUpdatesViewModel() {
        val viewModel = LoginViewModel()

        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel, onSuccess = {})
        }

        composeTestRule.onNodeWithTag("LoginEmail").performTextInput("user@example.com")
        composeTestRule.onNodeWithTag("LoginPassword").performTextInput("password123")

        TestCase.assertEquals("user@example.com", viewModel.email)
        TestCase.assertEquals("password123", viewModel.password)
    }

    @Test
    fun loginScreen_showsErrorWhenInputsInvalid() {
        val viewModel = LoginViewModel()

        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel, onSuccess = {})
        }

        composeTestRule.onNodeWithTag("LoginButton").performClick()

        composeTestRule.onNodeWithTag("LoginError")
            .assertIsDisplayed()
            .assertTextEquals("Invalid email or password")
    }

    @Test
    fun registerScreen_inputUpdatesViewModel() {
        val viewModel = RegisterViewModel()

        composeTestRule.setContent {
            RegisterScreen(viewModel = viewModel, onSuccess = {})
        }

        composeTestRule.onNodeWithTag("RegisterEmail").performTextInput("user@example.com")
        composeTestRule.onNodeWithTag("RegisterPassword").performTextInput("password123")

        TestCase.assertEquals("user@example.com", viewModel.email)
        TestCase.assertEquals("password123", viewModel.password)
    }

    @Test
    fun registerScreen_showsErrorWhenInputsInvalid() {
        val viewModel = RegisterViewModel()

        composeTestRule.setContent {
            RegisterScreen(viewModel = viewModel, onSuccess = {})
        }
        composeTestRule.onNodeWithTag("RegisterButton").performClick()

        composeTestRule.onNodeWithTag("RegisterError")
            .assertIsDisplayed()
            .assertTextEquals("Invalid email or password")
    }

}