package com.example.data.repository

import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.data.Milestone
import com.example.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class HabitRepositoryImpl : HabitRepository {
    private val habits = MutableStateFlow<List<Habit>>(emptyList())
    private val checkIns = MutableStateFlow<List<CheckIn>>(emptyList())
    private val milestones = MutableStateFlow<List<Milestone>>(emptyList())
    private val subscriptionTier = MutableStateFlow("free")

    private val db = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val encouragementPhrases = listOf(
        "You've kept this growing.",
        "Every drop counts.",
        "Mindful and consistent.",
        "Incredible dedication."
    )

    override fun getHabits(): Flow<List<Habit>> = habits

    override fun getSubscriptionTier(): Flow<String> = subscriptionTier

    override suspend fun updateSubscriptionTier(tier: String) {
        subscriptionTier.value = tier.lowercase()
    }

    override fun canAddHabit(): Flow<Boolean> = habits.map { habitList ->
        val activeCount = habitList.count { !it.archived }
        if (subscriptionTier.value == "free") {
            activeCount < 3
        } else {
            true
        }
    }

    override fun getRecentCheckIns(habitId: String): Flow<List<CheckIn>> = checkIns.map { list ->
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

    private fun calculateStreakAtDate(habit: Habit, date: LocalDate): Int {
        val checkInDates = checkIns.value
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
            streak >= 2 -> "sprout"
            else -> "seed"
        }
    }

    override suspend fun toggleCheckIn(habitId: String): Milestone? {
        val habit = habits.value.find { it.habitId == habitId } ?: return null
        val today = LocalDate.now()
        val todayStr = today.toString()
        
        val existingCheckIn = checkIns.value.find { it.habitId == habitId && it.date == todayStr }
        
        if (existingCheckIn != null) {
            // UNDO
            checkIns.value = checkIns.value.filter { it != existingCheckIn }
            
            val lastCheckIn = checkIns.value
                .filter { it.habitId == habitId && it.date < todayStr }
                .maxByOrNull { it.date }
                
            val newCurrentStreak = if (lastCheckIn != null) {
                getStreakAt(habit, LocalDate.parse(lastCheckIn.date), 
                    checkIns.value.filter { it.habitId == habitId }.map { LocalDate.parse(it.date) }.toSet())
            } else {
                0
            }
            
            val updatedHabit = habit.copy(currentStreak = newCurrentStreak)
            habits.value = habits.value.map { if (it.habitId == habitId) updatedHabit else it }
            
            db?.collection("check_ins")?.document(existingCheckIn.checkInId)?.delete()
            return null
        } else {
            // CHECK IN
            val newStreak = calculateStreakAtDate(habit, today)
            val status = getGrowthStage(newStreak)
            
            val newCheckIn = CheckIn(
                habitId = habitId,
                userId = "local_user",
                date = todayStr,
                status = status
            )
            checkIns.value = checkIns.value + newCheckIn
            
            val newLongestStreak = maxOf(habit.longestStreak, newStreak)
            val updatedHabit = habit.copy(
                currentStreak = newStreak,
                longestStreak = newLongestStreak
            )
            habits.value = habits.value.map { if (it.habitId == habitId) updatedHabit else it }
            
            db?.collection("check_ins")?.document(newCheckIn.checkInId)?.set(newCheckIn)
            
            if (newStreak in listOf(7, 30, 100)) {
                val milestone = Milestone(
                    habitId = habitId,
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
        val existing = habits.value.find { it.habitId == habit.habitId }
        if (existing != null) {
            habits.value = habits.value.map { if (it.habitId == habit.habitId) habit else it }
        } else {
            habits.value = habits.value + habit.copy(userId = "local_user")
        }
    }

    override suspend fun initializeDefaultHabits() {
        if (habits.value.isEmpty()) {
            val defaultHabits = listOf(
                Habit(
                    habitId = UUID.randomUUID().toString(),
                    userId = "local_user",
                    title = "Drink Water", targetText = "6 glasses",
                    iconKey = "water",
                    colorTag = "moss",
                    sortOrder = 0
                ),
                Habit(
                    habitId = UUID.randomUUID().toString(),
                    userId = "local_user",
                    title = "Read 20 Minutes",
                    iconKey = "read",
                    colorTag = "dustyClay",
                    sortOrder = 1
                ),
                Habit(
                    habitId = UUID.randomUUID().toString(),
                    userId = "local_user",
                    title = "Meditate", targetText = "10 min",
                    iconKey = "meditate",
                    colorTag = "inkTeal",
                    sortOrder = 2
                )
            )
            habits.value = defaultHabits
        }
    }

    override suspend fun updateHabitOrder(habitIds: List<String>) {
        habits.value = habits.value.map { habit ->
            val index = habitIds.indexOf(habit.habitId)
            if (index != -1) habit.copy(sortOrder = index) else habit
        }
    }

    override fun getWeeklyCheckIns(days: Int): Flow<List<CheckIn>> = checkIns.map { list ->
        val cutoffDate = LocalDate.now().minusDays(days.toLong())
        list.filter { checkIn ->
            try {
                LocalDate.parse(checkIn.date).isAfter(cutoffDate) || LocalDate.parse(checkIn.date).isEqual(cutoffDate)
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun getMilestones(): Flow<List<Milestone>> = milestones

    override suspend fun saveMilestone(milestone: Milestone) {
        // Save to local flow for immediate UI update
        milestones.value = (milestones.value + milestone).sortedByDescending { it.dateReached }
        
        // Save to Firestore if available
        db?.collection("milestones")?.add(milestone)
    }

    override suspend fun deleteHabitPermanently(habitId: String) {
        habits.value = habits.value.filter { it.habitId != habitId }
        checkIns.value = checkIns.value.filter { it.habitId != habitId }
        milestones.value = milestones.value.filter { it.habitId != habitId }

        db?.collection("habits")?.document(habitId)?.delete()
        db?.collection("check_ins")?.whereEqualTo("habitId", habitId)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
        db?.collection("milestones")?.whereEqualTo("habitId", habitId)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
    }
}
