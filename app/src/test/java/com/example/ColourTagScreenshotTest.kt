package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.Habit
import com.example.ui.components.LocalPlantMotionEnabled
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.PerennialTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Today with one habit per colour tag, half of them checked — the screen that proves the tag is
 * actually rendered rather than merely stored.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8)
class ColourTagScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val habits = listOf(
    "moss" to "Drink Water",
    "dustyClay" to "Read 20 Minutes",
    "butter" to "Stretch",
    "soil" to "Walk Outside",
    "inkTeal" to "Meditate",
    "charcoalInk" to "Journal"
  ).mapIndexed { index, (tag, title) ->
    Habit(
      habitId = "habit-$index",
      title = title,
      colorTag = tag,
      currentStreak = listOf(9, 6, 4, 3, 2, 1)[index],
      sortOrder = index
    )
  }

  @Test
  fun today_with_every_colour_tag() {
    composeTestRule.setContent {
      CompositionLocalProvider(LocalPlantMotionEnabled provides false) {
        PerennialTheme {
          TodayScreen(
            userName = "Friend",
            habits = habits,
            allCheckIns = emptyList(),
            onAddHabit = {},
            onHabitClick = {},
            onCheckIn = {},
            onNotificationsClick = {},
            onNavigateToTab = {},
            hourProvider = { 9 }
          )
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/today_colour_tags.png")
  }
}
