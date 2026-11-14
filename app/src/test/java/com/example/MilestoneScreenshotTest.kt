package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.LocalPlantMotionEnabled
import com.example.ui.screens.MilestoneCelebration
import com.example.ui.theme.PerennialTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8)
class MilestoneScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun render(streak: Int, title: String, encouragement: String, file: String) {
    composeTestRule.setContent {
      CompositionLocalProvider(LocalPlantMotionEnabled provides false) {
        PerennialTheme {
          MilestoneCelebration(
            streak = streak,
            habitTitle = title,
            encouragement = encouragement,
            onShare = {},
            onDismiss = {}
          )
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = file)
  }

  @Test
  fun milestone_seven_days() {
    render(7, "Drink Water", "A full week. The roots are in.", "src/test/screenshots/milestone_7.png")
  }

  @Test
  fun milestone_hundred_days() {
    render(100, "Read 20 Minutes", "One hundred days of showing up.", "src/test/screenshots/milestone_100.png")
  }
}
