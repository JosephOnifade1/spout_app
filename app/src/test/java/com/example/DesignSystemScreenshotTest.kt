package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.components.PlantFraming
import com.example.ui.components.PlantPortrait
import com.example.ui.components.PlantStage
import com.example.ui.theme.Moss
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PaperSage
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
 * Renders the design-system pieces to PNG so the look can be reviewed from a CI artifact
 * instead of a device or a preview tunnel. Run with:
 *
 *   ./gradlew :app:recordRoborazziDebug --tests "com.example.DesignSystemScreenshotTest"
 *
 * Output lands in app/src/test/screenshots/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8)
class DesignSystemScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun plant_stages_card_framing() {
    composeTestRule.setContent {
      PerennialTheme {
        Surface(color = PaperSage) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            PlantStage.entries.forEach { stage ->
              PlantPortrait(
                stage = stage,
                size = 64.dp,
                framing = PlantFraming.CARD,
                animated = false
              )
            }
          }
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/plant_stages_card.png")
  }

  @Test
  fun plant_stages_full_framing() {
    composeTestRule.setContent {
      PerennialTheme {
        Surface(color = PaperCream) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
          ) {
            PlantStage.entries.forEach { stage ->
              PlantPortrait(
                stage = stage,
                size = 72.dp,
                framing = PlantFraming.FULL,
                animated = false
              )
            }
          }
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/plant_stages_full.png")
  }

  @Test
  fun habit_card_checked_and_unchecked() {
    composeTestRule.setContent {
      PerennialTheme {
        Surface(color = PaperSage) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            HabitCardMock(title = "Drink Water", meta = "Goal achieved!", stage = PlantStage.BLOOM, checked = true)
            HabitCardMock(title = "Read 20 Minutes", meta = "Check in", stage = PlantStage.BUD, checked = false)
            HabitCardMock(title = "Meditate", meta = "10 min", stage = PlantStage.SPROUT, checked = false)
            HabitCardMock(title = "Morning Pages", meta = "Plant your first habit", stage = PlantStage.SEED, checked = false)
          }
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/habit_cards.png")
  }

  @Test
  fun typography_specimen() {
    composeTestRule.setContent {
      PerennialTheme {
        Surface(color = PaperCream) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text("You've nurtured this for 7 days", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text("Fraunces for display. Public Sans for body copy that gets out of the way.",
                 style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Text("007 DAYS · BLOOM · MOSS", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/typography.png")
  }

  @Composable
  private fun HabitCardMock(
    title: String,
    meta: String,
    stage: PlantStage,
    checked: Boolean
  ) {
    Surface(
      color = PaperCream,
      shape = RoundedCornerShape(18.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = if (checked) PaperSage else Color(0xFFF7F3E6),
          shape = RoundedCornerShape(15.dp)
        ) {
          PlantPortrait(stage = stage, size = 60.dp, framing = PlantFraming.CARD, animated = false)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(title, style = MaterialTheme.typography.titleMedium)
          Text(
            meta,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) Moss else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(Modifier.width(8.dp))
        Surface(
          color = if (checked) Moss else Moss.copy(alpha = 0.12f),
          shape = RoundedCornerShape(percent = 50),
          modifier = Modifier.size(38.dp)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (checked) "✓" else "+",
              style = MaterialTheme.typography.titleMedium,
              color = if (checked) Color.White else Moss
            )
          }
        }
      }
    }
  }
}
