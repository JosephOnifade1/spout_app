package com.example.data.repository

import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.data.Milestone
import com.example.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.content.Context
import android.app.Activity

import com.google.firebase.firestore.ListenerRegistration
import com.revenuecat.purchases.interfaces.LogInCallback

class HabitRepositoryImpl(
    private val habitDao: com.example.data.local.HabitDao,
    private val context: Context
) : HabitRepository {
    private val subscriptionTier = MutableStateFlow("free")
    private val userDisplayName = MutableStateFlow("Friend")
    private var userSnapshotListener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onUserChanged(userId: String?) {
        userSnapshotListener?.remove()
        userSnapshotListener = null

        val isRevenueCatConfigured = try {
            Purchases.isConfigured
        } catch (e: Exception) {
            false
        }

        if (userId != null) {
            // RevenueCat login
            if (isRevenueCatConfigured) {
                try {
                    Purchases.sharedInstance.logIn(userId, object : LogInCallback {
                        override fun onError(error: PurchasesError) {}
                        override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                            updateSubscriptionFromCustomerInfo(customerInfo)
                        }
                    })
                } catch (e: Exception) {
                    // Fail silently
                }
            }

            // Firestore observer
            userSnapshotListener = db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val tier = snapshot.getString("subscriptionTier") ?: "free"
                    subscriptionTier.value = tier

                    val nameFromFirestore = snapshot.getString("display_name") ?: snapshot.getString("displayName")
                    val auth = FirebaseAuth.getInstance()
                    val fallbackName = auth.currentUser?.displayName
                    userDisplayName.value = nameFromFirestore ?: fallbackName ?: "Friend"
                } else {
                    val auth = FirebaseAuth.getInstance()
                    userDisplayName.value = auth.currentUser?.displayName ?: "Friend"
                }
            }
        } else {
            // RevenueCat logout
            if (isRevenueCatConfigured) {
                try {
                    Purchases.sharedInstance.logOut(null)
                } catch (e: Exception) {
                    // Fail silently
                }
            }
            subscriptionTier.value = "free"
            userDisplayName.value = "Friend"
        }
    }

    private val encouragementPhrases = listOf(
        "You've kept this growing.",
        "Every drop counts.",
        "Mindful and consistent.",
        "Incredible dedication."
    )

    override fun getHabits(): Flow<List<Habit>> {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        return habitDao.getAllHabits().map { list ->
            list.filter { it.userId == userId }
        }
    }

    override fun getSubscriptionTier(): Flow<String> = subscriptionTier
    override fun getUserDisplayName(): Flow<String> = userDisplayName

    override suspend fun updateSubscriptionTier(tier: String) {
        subscriptionTier.value = tier.lowercase()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        if (userId != "local_user") {
            db.collection("users").document(userId).update("subscriptionTier", tier.lowercase())
        }
    }

    override suspend fun getOfferings(): Offerings? {
        val isConfigured = try { Purchases.isConfigured } catch (e: Exception) { false }
        if (!isConfigured) return null

        return suspendCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                    override fun onError(error: PurchasesError) {
                        continuation.resume(null)
                    }
                    override fun onReceived(offerings: Offerings) {
                        continuation.resume(offerings)
                    }
                })
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    override suspend fun purchasePackage(activity: Activity, packageToPurchase: Package): Result<Unit> {
        val isConfigured = try { Purchases.isConfigured } catch (e: Exception) { false }
        if (!isConfigured) return Result.failure(Exception("Payments not configured"))

        return suspendCoroutine { continuation ->
            try {
                Purchases.sharedInstance.purchasePackage(
                    activity = activity,
                    packageToPurchase = packageToPurchase,
                    listener = object : PurchaseCallback {
                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                        override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                            updateSubscriptionFromCustomerInfo(customerInfo)
                            continuation.resume(Result.success(Unit))
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun syncSubscriptionStatus() {
        val isConfigured = try { Purchases.isConfigured } catch (e: Exception) { false }
        if (!isConfigured) return

        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onError(error: PurchasesError) {
                    // Handled by caller or ignored
                }
                override fun onReceived(customerInfo: CustomerInfo) {
                    updateSubscriptionFromCustomerInfo(customerInfo)
                }
            })
        } catch (e: Exception) {
            // SDK might not be configured yet
        }
    }

    override fun updateSubscriptionFromCustomerInfo(customerInfo: CustomerInfo) {
        val hasPlus = customerInfo.entitlements["Perennial Pro"]?.isActive == true
        val tier = if (hasPlus) "plus" else "free"
        subscriptionTier.value = tier
        
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        if (userId != "local_user") {
            db.collection("users").document(userId).update("subscriptionTier", tier)
                .addOnFailureListener {
                    db.collection("users").document(userId).set(mapOf("subscriptionTier" to tier))
                }
        }
    }

    override fun canAddHabit(): Flow<Boolean> = combine(getHabits(), subscriptionTier) { habitList, tier ->
        val activeCount = habitList.count { !it.archived }
        if (tier == "free") {
            activeCount < 3
        } else {
            true
        }
    }

    override fun getRecentCheckIns(habitId: String): Flow<List<CheckIn>> = habitDao.getAllCheckIns().map { list ->
        list.filter { it.habitId == habitId }.sortedBy { it.date }.takeLast(7)
    }

    private fun isScheduled(habit: Habit, date: LocalDate): Boolean {
        return when (habit.frequency.lowercase()) {
            "daily" -> true
            "weekdays" -> date.dayOfWeek.value in 1..5
            "custom" -> habit.customDays.contains(date.dayOfWeek.value)
            else -> true
        }
    }

    private fun getPreviousScheduledDate(habit: Habit, date: LocalDate): LocalDate {
        var prev = date.minusDays(1)
        val createdAt = Instant.ofEpochMilli(habit.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        // Don't look back before creation
        while (!isScheduled(habit, prev) && prev.isAfter(createdAt)) {
            prev = prev.minusDays(1)
        }
        return prev
    }

    private fun getStreakAt(habit: Habit, date: LocalDate, allCheckInDates: Set<LocalDate>): Int {
        var current = date
        var streak = 1
        val createdAt = Instant.ofEpochMilli(habit.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        
        while (true) {
            val prev = getPreviousScheduledDate(habit, current)
            if (prev.isBefore(createdAt)) break
            
            if (allCheckInDates.contains(prev)) {
                streak++
                current = prev
            } else {
                val prevPrev = getPreviousScheduledDate(habit, prev)
                if (prevPrev.isBefore(createdAt)) break
                
                if (allCheckInDates.contains(prevPrev)) {
                    streak++
                    current = prevPrev
                } else {
                    break
                }
            }
        }
        return streak
    }

    private fun calculateStreakAtDate(habit: Habit, date: LocalDate, currentCheckIns: List<CheckIn>): Int {
        val checkInDates = currentCheckIns
            .filter { it.habitId == habit.habitId && it.date < date.toString() }
            .map { LocalDate.parse(it.date) }
            .toSet()
            
        if (checkInDates.isEmpty()) return 1
        
        val createdAt = Instant.ofEpochMilli(habit.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        val prevScheduled = getPreviousScheduledDate(habit, date)
        
        if (prevScheduled.isBefore(createdAt)) return 1
 
        if (checkInDates.contains(prevScheduled)) {
            return getStreakAt(habit, prevScheduled, checkInDates) + 1
        } else {
            val prevPrevScheduled = getPreviousScheduledDate(habit, prevScheduled)
            if (prevPrevScheduled.isBefore(createdAt)) return 1
            
            if (checkInDates.contains(prevPrevScheduled)) {
                return getStreakAt(habit, prevPrevScheduled, checkInDates) + 1
            } else {
                return 1
            }
        }
    }

    private fun getGrowthStage(streak: Int): String {
        return when {
            streak >= 7 -> "bloom"
            streak >= 4 -> "bud"
            streak >= 2 -> "perennial"
            else -> "seed"
        }
    }

    override suspend fun toggleCheckIn(habitId: String): Milestone? {
        val habitsList = habitDao.getAllHabits().first()
        val checkInsList = habitDao.getAllCheckIns().first()
        
        val habit = habitsList.find { it.habitId == habitId } ?: return null
        val today = LocalDate.now()
        val todayStr = today.toString()
        
        val existingCheckIn = checkInsList.find { it.habitId == habitId && it.date == todayStr }
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        
        if (existingCheckIn != null) {
            // UNDO
            habitDao.deleteCheckIn(existingCheckIn)
            db.collection("check_ins").document(existingCheckIn.checkInId).delete()
            
            val remainingCheckIns = checkInsList.filter { it != existingCheckIn }
            val lastCheckIn = remainingCheckIns
                .filter { it.habitId == habitId && it.date < todayStr }
                .maxByOrNull { it.date }
                
            val newCheckInDates = remainingCheckIns.filter { it.habitId == habitId }.map { LocalDate.parse(it.date) }.toSet()
            val newCurrentStreak = if (lastCheckIn != null) {
                getStreakAt(habit, LocalDate.parse(lastCheckIn.date), newCheckInDates)
            } else {
                0
            }
            
            val updatedHabit = habit.copy(currentStreak = newCurrentStreak)
            habitDao.insertHabit(updatedHabit)
            db.collection("habits").document(habitId).set(updatedHabit)
            
            return null
        } else {
            // CHECK IN
            val newStreak = calculateStreakAtDate(habit, today, checkInsList)
            val status = getGrowthStage(newStreak)
            
            val newCheckIn = CheckIn(
                habitId = habitId,
                userId = userId,
                date = todayStr,
                status = status
            )
            habitDao.insertCheckIn(newCheckIn)
            db.collection("check_ins").document(newCheckIn.checkInId).set(newCheckIn)
            
            val newLongestStreak = maxOf(habit.longestStreak, newStreak)
            val updatedHabit = habit.copy(
                currentStreak = newStreak,
                longestStreak = newLongestStreak
            )
            habitDao.insertHabit(updatedHabit)
            db.collection("habits").document(habitId).set(updatedHabit)
            
            if (newStreak in listOf(7, 30, 100)) {
                val milestone = Milestone(
                    habitId = habitId,
                    userId = userId,
                    habitTitle = habit.title,
                    tier = newStreak,
                    dateReached = today.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    encouragementLine = encouragementPhrases.random()
                )
                saveMilestone(milestone)
                return milestone
            }
            return null
        }
    }

    override suspend fun saveHabit(habit: Habit) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        val habitToSave = habit.copy(userId = userId)
        habitDao.insertHabit(habitToSave)
        db.collection("habits").document(habitToSave.habitId).set(habitToSave)
        try {
            com.example.notifications.ReminderScheduler.scheduleReminder(context, habitToSave)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun initializeDefaultHabits() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // 1. Check if user document exists in Firestore
        val userDocRef = db.collection("users").document(userId)
        val userDoc = suspendCoroutine { continuation ->
            userDocRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resume(null)
                }
            }
        }

        if (userDoc == null || !userDoc.exists()) {
            // First time sign-in: create user doc in Firestore
            val sharedPrefs = context.getSharedPreferences("perennial_settings", Context.MODE_PRIVATE)
            val savedReminderStyle = sharedPrefs.getString("reminder_style", "Voice") ?: "Voice"
            val isDark = sharedPrefs.getBoolean("dark_mode", false)
            val themeStr = if (isDark) "Dark (Ink Teal)" else "Light (Sage)"

            val initialUserData = mapOf(
                "uid" to userId,
                "display_name" to (auth.currentUser?.displayName ?: "Friend"),
                "displayName" to (auth.currentUser?.displayName ?: "Friend"),
                "email" to (auth.currentUser?.email ?: ""),
                "reminder_style" to savedReminderStyle,
                "reminderStyle" to savedReminderStyle,
                "theme" to themeStr,
                "subscription_tier" to "free",
                "subscriptionTier" to "free",
                "created_at" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis()
            )
            suspendCoroutine { continuation ->
                userDocRef.set(initialUserData).addOnCompleteListener {
                    continuation.resume(Unit)
                }
            }

            // Seed the 3 default habits if none exist locally
            val localHabits = habitDao.getAllHabits().first()
            if (localHabits.isEmpty()) {
                val defaultHabits = listOf(
                    Habit(
                        habitId = UUID.randomUUID().toString(),
                        userId = userId,
                        title = "Drink Water", targetText = "6 glasses",
                        iconKey = "water",
                        colorTag = "moss",
                        sortOrder = 0
                    ),
                    Habit(
                        habitId = UUID.randomUUID().toString(),
                        userId = userId,
                        title = "Read 20 Minutes",
                        iconKey = "read",
                        colorTag = "dustyClay",
                        sortOrder = 1
                    ),
                    Habit(
                        habitId = UUID.randomUUID().toString(),
                        userId = userId,
                        title = "Meditate", targetText = "10 min",
                        iconKey = "meditate",
                        colorTag = "inkTeal",
                        sortOrder = 2
                    )
                )
                for (habit in defaultHabits) {
                    habitDao.insertHabit(habit)
                    db.collection("habits").document(habit.habitId).set(habit)
                }
            }
        } else {
            // Returning user: Load existing data from Firestore to local Room
            // A. Sync habits
            val habitsTask = suspendCoroutine { continuation ->
                db.collection("habits").whereEqualTo("userId", userId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
            if (habitsTask != null) {
                for (doc in habitsTask.documents) {
                    val habit = doc.toObject(Habit::class.java)
                    if (habit != null) {
                        habitDao.insertHabit(habit)
                    }
                }
            }

            // B. Sync check-ins
            val checkInsTask = suspendCoroutine { continuation ->
                db.collection("check_ins").whereEqualTo("userId", userId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
            if (checkInsTask != null) {
                for (doc in checkInsTask.documents) {
                    val checkIn = doc.toObject(CheckIn::class.java)
                    if (checkIn != null) {
                        habitDao.insertCheckIn(checkIn)
                    }
                }
            }

            // C. Sync milestones
            val milestonesTask = suspendCoroutine { continuation ->
                db.collection("milestones").whereEqualTo("userId", userId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
            if (milestonesTask != null) {
                for (doc in milestonesTask.documents) {
                    val milestone = doc.toObject(Milestone::class.java)
                    if (milestone != null) {
                        habitDao.insertMilestone(milestone)
                    }
                }
            }
        }
    }

    override suspend fun updateHabitOrder(habitIds: List<String>) {
        val habitsList = habitDao.getAllHabits().first()
        for (habit in habitsList) {
            val index = habitIds.indexOf(habit.habitId)
            if (index != -1) {
                val updated = habit.copy(sortOrder = index)
                habitDao.insertHabit(updated)
                db.collection("habits").document(habit.habitId).update("sortOrder", index)
            }
        }
    }

    override fun getWeeklyCheckIns(days: Int): Flow<List<CheckIn>> = habitDao.getAllCheckIns().map { list ->
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        val cutoffDate = LocalDate.now().minusDays(days.toLong())
        list.filter { checkIn ->
            checkIn.userId == userId && try {
                val checkInDate = LocalDate.parse(checkIn.date)
                checkInDate.isAfter(cutoffDate) || checkInDate.isEqual(cutoffDate)
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun getMilestones(): Flow<List<Milestone>> {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "local_user"
        return habitDao.getAllMilestones().map { list ->
            list.filter { it.userId == userId }
        }
    }

    override suspend fun saveMilestone(milestone: Milestone) {
        habitDao.insertMilestone(milestone)
        db.collection("milestones").document(milestone.milestoneId).set(milestone)
    }

    override suspend fun deleteHabitPermanently(habitId: String) {
        habitDao.deleteHabitById(habitId)
        habitDao.deleteMilestonesByHabitId(habitId)
        // Note: we'd need a DAO method to delete checkins by habitId if we want to be thorough
        
        try {
            com.example.notifications.ReminderScheduler.cancelReminder(context, habitId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        db.collection("habits").document(habitId).delete()
        db.collection("check_ins").whereEqualTo("habitId", habitId).get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
        db.collection("milestones").whereEqualTo("habitId", habitId).get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
    }
}
