package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.CheckIn
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
import java.time.LocalDate

/**
 * Renders Today in its real, data-driven states so the screen can be reviewed from CI.
 *
 * Ambient motion is disabled here via [LocalPlantMotionEnabled]: an infinite animation never
 * lets Compose reach idle, so the capture would otherwise hang.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8)
class TodayScreenScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun habit(
    title: String,
    streak: Int,
    target: String?,
    icon: String,
    sortOrder: Int
  ) = Habit(
    habitId = "habit-$sortOrder",
    title = title,
    targetText = target,
    iconKey = icon,
    currentStreak = streak,
    sortOrder = sortOrder
  )

  private val sampleHabits = listOf(
    habit("Drink Water", streak = 8, target = "6 glasses", icon = "water", sortOrder = 0),
    habit("Read 20 Minutes", streak = 5, target = "20 pages", icon = "read", sortOrder = 1),
    habit("Meditate", streak = 3, target = "10 min", icon = "meditate", sortOrder = 2),
    habit("Morning Pages", streak = 0, target = null, icon = "exercise", sortOrder = 3)
  )

  private fun renderToday(habits: List<Habit>, checkIns: List<CheckIn>) {
    composeTestRule.setContent {
      CompositionLocalProvider(LocalPlantMotionEnabled provides false) {
        PerennialTheme {
          TodayScreen(
            userName = "Friend",
            habits = habits,
            allCheckIns = checkIns,
            onAddHabit = {},
            onHabitClick = {},
            onCheckIn = {},
            onNotificationsClick = {},
            onNavigateToTab = {}
          )
        }
      }
    }
  }

  @Test
  fun today_mixed_states() {
    val today = LocalDate.now().toString()
    renderToday(
      habits = sampleHabits,
      checkIns = listOf(
        CheckIn(habitId = "habit-0", date = today),
        CheckIn(habitId = "habit-2", date = today)
      )
    )
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/today_mixed.png")
  }

  @Test
  fun today_nothing_checked_yet() {
    renderToday(habits = sampleHabits, checkIns = emptyList())
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/today_fresh.png")
  }

  @Test
  fun today_empty_state() {
    renderToday(habits = emptyList(), checkIns = emptyList())
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/today_empty.png")
  }
}
