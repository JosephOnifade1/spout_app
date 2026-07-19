package com.example.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.Habit
import com.example.data.local.PerennialDatabase
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val TAG_PREFIX = "habit_reminder_"

    fun scheduleReminder(context: Context, habit: Habit) {
        if (!habit.reminderEnabled || habit.archived) {
            cancelReminder(context, habit.habitId)
            return
        }

        val (hour, minute) = parseReminderTime(habit.reminderTime)
        
        val now = LocalDateTime.now()
        var targetTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hour, minute))
        
        // If the target time is in the past for today, schedule for tomorrow
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMillis = Duration.between(now, targetTime).toMillis()
        
        // Build the WorkRequest
        val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInputData(HabitReminderWorker.createInputData(habit.habitId))
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(TAG_PREFIX + habit.habitId)
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            TAG_PREFIX + habit.habitId,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context, habitId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(TAG_PREFIX + habitId)
    }

    suspend fun rescheduleAllReminders(context: Context) {
        val db = PerennialDatabase.getDatabase(context)
        val habits = db.habitDao().getAllHabitsList()
        for (habit in habits) {
            scheduleReminder(context, habit)
        }
    }

    private fun parseReminderTime(timeStr: String): Pair<Int, Int> {
        val trimmed = timeStr.trim()
        val isPm = trimmed.endsWith("PM", ignoreCase = true)
        val isAm = trimmed.endsWith("AM", ignoreCase = true)
        
        val cleanTime = trimmed.replace("PM", "", ignoreCase = true)
                               .replace("AM", "", ignoreCase = true)
                               .trim()
                               
        val parts = cleanTime.split(":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30
        
        if (isPm && hour < 12) {
            hour += 12
        } else if (isAm && hour == 12) {
            hour = 0
        }
        
        return Pair(hour, minute)
    }
}
