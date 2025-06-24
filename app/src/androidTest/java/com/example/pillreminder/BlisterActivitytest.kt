package com.example.pillreminder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate

/**
 * Instrumented UI tests for [BlisterActivity]
 *
 * The tests use Mockito to simulate FirebaseAuth user states
 */
@RunWith(AndroidJUnit4::class)
class BlisterActivityTest {

    /**
     * Compose testing rule that provides access to the activity context and Compose testing APIs
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Checking the UI view when the user is not logged in
     */
    @Test
    fun pillScreen_ShowsLoginMessage_WhenUserNotLoggedIn() {
        val mockAuth = mock(FirebaseAuth::class.java)
        `when`(mockAuth.currentUser).thenReturn(null)

        composeTestRule.setContent {
            PillScreen(auth = mockAuth)
        }

        composeTestRule.onNodeWithText("Log in or register to unlock all features!")
            .assertIsDisplayed()
    }

    /**
     * Checking data formatting
     */
    @Test
    fun testDateFormatting() {
        val date = LocalDate.of(2025, 6, 15)
        val formatted = dateFormatting(date)
        assertEquals("15 Jun", formatted)
    }

    /**
     * Checking the presence of the header
     */
    @Test
    fun currentBlisterText_DisplaysHeadline() {
        composeTestRule.setContent {
            CurrentBlisterText()
        }

        composeTestRule
            .onNodeWithText("Your current blister:", substring = true)
            .assertExists()
    }
}