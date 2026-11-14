package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Habit
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.getColorFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config()
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Perennial", appName)
  }

  @Test
  fun `test color tag swatch mapping matches exact hex design tokens`() {
    // Moss: #5B7F5E
    assertEquals(Color(0xFF5B7F5E), getColorFromString("moss"))
    assertEquals(Color(0xFF5B7F5E), getColorFromString("MOSS"))

    // Dusty Clay: #C97B84
    assertEquals(Color(0xFFC97B84), getColorFromString("dustyClay"))
    assertEquals(Color(0xFFC97B84), getColorFromString("dustyclay"))

    // Butter: #F0C868
    assertEquals(Color(0xFFF0C868), getColorFromString("butter"))

    // Soil: #8C7A6B
    assertEquals(Color(0xFF8C7A6B), getColorFromString("soil"))

    // Ink Teal: #1B2E2B
    assertEquals(Color(0xFF1B2E2B), getColorFromString("inkTeal"))
    assertEquals(Color(0xFF1B2E2B), getColorFromString("inkteal"))

    // Charcoal Ink: #2B2B28
    assertEquals(Color(0xFF2B2B28), getColorFromString("charcoalInk"))
    assertEquals(Color(0xFF2B2B28), getColorFromString("charcoalink"))
  }

  @Test
  fun `test custom habit creation and attributes`() {
    val customHabit = Habit(
        title = "Water Ferns",
        iconKey = "water",
        colorTag = "moss",
        frequency = "custom",
        customDays = listOf(1, 3, 5), // Mon, Wed, Fri
        reminderTime = "08:00 AM",
        reminderEnabled = true
    )

    assertEquals("Water Ferns", customHabit.title)
    assertEquals("water", customHabit.iconKey)
    assertEquals("moss", customHabit.colorTag)
    assertEquals("custom", customHabit.frequency)
    assertEquals(listOf(1, 3, 5), customHabit.customDays)
    assertEquals("08:00 AM", customHabit.reminderTime)
    assertTrue(customHabit.reminderEnabled)
  }

  @Test
  fun `test habit scheduling logic`() {
    // 1. Daily frequency
    val dailyHabit = Habit(frequency = "daily")
    assertTrue(isScheduled(dailyHabit, LocalDate.of(2026, 7, 20))) // Monday
    assertTrue(isScheduled(dailyHabit, LocalDate.of(2026, 7, 25))) // Saturday

    // 2. Weekdays frequency
    val weekdaysHabit = Habit(frequency = "weekdays")
    assertTrue(isScheduled(weekdaysHabit, LocalDate.of(2026, 7, 20))) // Monday (day 1)
    assertTrue(isScheduled(weekdaysHabit, LocalDate.of(2026, 7, 24))) // Friday (day 5)
    assertFalse(isScheduled(weekdaysHabit, LocalDate.of(2026, 7, 25))) // Saturday (day 6)
    assertFalse(isScheduled(weekdaysHabit, LocalDate.of(2026, 7, 26))) // Sunday (day 7)

    // 3. Custom frequency
    val customHabit = Habit(frequency = "custom", customDays = listOf(2, 4)) // Tue, Thu
    assertFalse(isScheduled(customHabit, LocalDate.of(2026, 7, 20))) // Monday
    assertTrue(isScheduled(customHabit, LocalDate.of(2026, 7, 21))) // Tuesday
    assertFalse(isScheduled(customHabit, LocalDate.of(2026, 7, 22))) // Wednesday
    assertTrue(isScheduled(customHabit, LocalDate.of(2026, 7, 23))) // Thursday
    assertFalse(isScheduled(customHabit, LocalDate.of(2026, 7, 24))) // Friday
  }

  private fun isScheduled(habit: Habit, date: LocalDate): Boolean {
    return when (habit.frequency.lowercase()) {
      "daily" -> true
      "weekdays" -> date.dayOfWeek.value in 1..5
      "custom" -> habit.customDays.contains(date.dayOfWeek.value)
      else -> true
    }
  }
}
