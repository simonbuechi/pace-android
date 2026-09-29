package ch.simibu.pace

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.simibu.pace.ui.components.TactileCard
import ch.simibu.pace.ui.components.TactilePillButton
import ch.simibu.pace.ui.components.TactileSunkenWell
import ch.simibu.pace.ui.theme.PaceAmigoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TactileComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTactileCardDisplaysContent() {
        composeTestRule.setContent {
            PaceAmigoTheme {
                TactileCard(modifier = Modifier.testTag("tactile_card")) {
                    Text("Pace Tactile Card")
                }
            }
        }

        composeTestRule.onNodeWithTag("tactile_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pace Tactile Card").assertIsDisplayed()
    }

    @Test
    fun testTactileSunkenWellDisplaysContent() {
        composeTestRule.setContent {
            PaceAmigoTheme {
                TactileSunkenWell(modifier = Modifier.testTag("sunken_well")) {
                    Text("Sunken Inset Area")
                }
            }
        }

        composeTestRule.onNodeWithTag("sunken_well").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunken Inset Area").assertIsDisplayed()
    }

    @Test
    fun testTactilePillButtonClick() {
        var clicked = 0

        composeTestRule.setContent {
            PaceAmigoTheme {
                TactilePillButton(
                    modifier = Modifier
                        .testTag("pill_btn")
                        .clickable { clicked++ }
                ) {
                    Text("Start Session")
                }
            }
        }

        composeTestRule.onNodeWithTag("pill_btn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Session").assertIsDisplayed()

        composeTestRule.onNodeWithTag("pill_btn").performClick()
        assertEquals(1, clicked)
    }
}
