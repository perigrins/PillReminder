package com.example.pillreminder

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appContent_displaysAllButtons() {
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pill tracker").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun clickingLoginButton_launchesLoginRegisterActivity() {
        composeTestRule.onNodeWithText("Login").performClick()
    }

    @Test
    fun clickingLoginButton_launchesBlisterActivity() {
        composeTestRule.onNodeWithText("Pill tracker").performClick()
    }

    @Test
    fun clickingLoginButton_launchesSettingsActivity() {
        composeTestRule.onNodeWithText("Settings").performClick()
    }
}
