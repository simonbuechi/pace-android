package ch.simibu.pace

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import ch.simibu.pace.ui.screens.QuickStartScreen
import ch.simibu.pace.ui.theme.PaceAmigoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuickStartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testQuickStartScreenRendersAndStartsSession() {
        var sessionStarted = false

        composeTestRule.setContent {
            PaceAmigoTheme {
                QuickStartScreen(
                    onStartSession = { sessionStarted = true }
                )
            }
        }

        // Verify key UI text elements are visible
        composeTestRule.onNodeWithText("Quick Starter").assertIsDisplayed()
        composeTestRule.onNodeWithText("Focus Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Break Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sets / Rounds").assertIsDisplayed()

        // Scroll down to Start Session and click it
        composeTestRule.onNodeWithText("Start Session").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Session").performClick()
        assertTrue("onStartSession callback should be triggered", sessionStarted)
    }
}
