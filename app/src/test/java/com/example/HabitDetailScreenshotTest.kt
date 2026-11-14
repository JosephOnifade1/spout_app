package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.CheckIn
import com.example.data.Habit
import com.example.ui.components.LocalPlantMotionEnabled
import com.example.ui.screens.HabitDetailScreen
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
 * Habit Detail in its two meaningful states: an established habit in bloom, and a newer one
 * still in bud. `today` is pinned so the week strip is identical on every run.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8)
class HabitDetailScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val today: LocalDate = LocalDate.of(2026, 11, 14)

  private fun render(habit: Habit, checkIns: List<CheckIn>) {
    composeTestRule.setContent {
      CompositionLocalProvider(LocalPlantMotionEnabled provides false) {
        PerennialTheme {
          HabitDetailScreen(
            habit = habit,
            recentCheckIns = checkIns,
            onBack = {},
            onEdit = {},
            onArchive = {},
            onUnarchive = {},
            today = today
          )
        }
      }
    }
  }

  private fun week(statuses: List<String?>): List<CheckIn> =
    statuses.mapIndexedNotNull { index, status ->
      status?.let {
        CheckIn(
          habitId = "habit-bloom",
          date = today.minusDays((statuses.size - 1 - index).toLong()).toString(),
          status = it
        )
      }
    }

  @Test
  fun habit_detail_in_bloom() {
    render(
      habit = Habit(
        habitId = "habit-bloom",
        title = "Drink Water",
        targetText = "6 glasses",
        currentStreak = 12,
        longestStreak = 18,
        frequency = "Daily",
        reminderTime = "07:30"
      ),
      checkIns = week(listOf("seed", "perennial", "bud", "bud", "bloom", "bloom", null))
    )
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habit_detail_bloom.png")
  }

  @Test
  fun habit_detail_in_bud() {
    render(
      habit = Habit(
        habitId = "habit-bud",
        title = "Read 20 Minutes",
        targetText = "20 pages",
        currentStreak = 5,
        frequency = "Weekdays",
        reminderTime = "21:00"
      ),
      checkIns = week(listOf("seed", "perennial", "bud", null, "bud", "bud", null))
    )
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habit_detail_bud.png")
  }
}
