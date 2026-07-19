package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.PerennialDatabase
import java.util.Random

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()
        
        val db = PerennialDatabase.getDatabase(context)
        val habit = db.habitDao().getHabitById(habitId) ?: return Result.success()
        
        if (!habit.reminderEnabled || habit.archived) {
            return Result.success()
        }
        
        val sharedPrefs = context.getSharedPreferences("perennial_settings", Context.MODE_PRIVATE)
        val reminderStyle = sharedPrefs.getString("reminder_style", "Voice") ?: "Voice"
        
        if (reminderStyle.equals("None", ignoreCase = true)) {
            // Reschedule next day's reminder and exit
            ReminderScheduler.scheduleReminder(context, habit)
            return Result.success()
        }
        
        // Pick rotating phrase
        val isVoice = reminderStyle.equals("Voice", ignoreCase = true)
        val phrase = if (isVoice) {
            VOICE_PHRASES[Random().nextInt(VOICE_PHRASES.size)]
        } else {
            TEXT_PHRASES[Random().nextInt(TEXT_PHRASES.size)]
        }
        
        val notificationTitle = habit.title
        val notificationText = phrase
        
        // Create Notification Channel (Safe to call repeatedly)
        createNotificationChannel()
        
        // Build the intent to open the app and optionally speak
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("speak_text", if (isVoice) phrase else null)
            putExtra("habit_id", habitId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // In app, we can use a custom icon or a system default icon.
        // android.R.drawable.ic_lock_idle_alarm is guaranteed to be present and available.
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        try {
            // Under Android 13+, check POST_NOTIFICATIONS runtime permission before posting
            if (Build.VERSION.SDK_INT < 33 || 
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(habitId.hashCode(), builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Crucial: reschedule for tomorrow to keep the reminder active!
        ReminderScheduler.scheduleReminder(context, habit)
        
        return Result.success()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Get gentle nudges to keep nurturing your habits."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val CHANNEL_ID = "habit_reminders_channel"
        private const val KEY_HABIT_ID = "key_habit_id"
        
        fun createInputData(habitId: String): Data {
            return Data.Builder()
                .putString(KEY_HABIT_ID, habitId)
                .build()
        }
        
        private val TEXT_PHRASES = listOf(
            "Your plant is waiting for a drop of water and some love!",
            "Time for a tiny growth moment. Let's check in on your habits.",
            "Nurture your habits, watch them bloom!",
            "Every small step brings you closer to your goals. Shall we?",
            "Be gentle with yourself, but take that small step today.",
            "A moment of mindfulness can transform your whole day.",
            "Take a deep breath. Your garden is ready to grow with you."
        )
        
        private val VOICE_PHRASES = listOf(
            "Hey there, Friend! Just a gentle check-in. Let's spend a quick, cozy moment nurturing your habits.",
            "A beautiful day to grow! Take a deep breath and tell me: are we ready to make today count?",
            "I'm so proud of your dedication. Let's take a tiny step forward together right now.",
            "Your virtual plant is sending you warm vibes. Shall we give it some attention today?",
            "Pause for a second. Inhale, exhale. Now, let's do this small action together.",
            "Just checking in on you! No pressure at all, but your habits would love some attention."
        )
    }
}
