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
import java.time.LocalDate
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

    private fun calculateGrowthStatus(points: Int): GrowthStatus {
        return when {
            points < 5 -> GrowthStatus.SEED
            points < 15 -> GrowthStatus.SPROUT
            points < 30 -> GrowthStatus.BUD
            else -> GrowthStatus.BLOOM
        }
    }

    override suspend fun checkIn(habit: Habit, date: String, status: GrowthStatus): Milestone? {
        val newPoints = habit.growthPoints + 1
        val newStatus = calculateGrowthStatus(newPoints)
        
        val newCheckIn = CheckIn(
            habitId = habit.habitId,
            userId = "local_user",
            date = date,
            status = newStatus.value
        )
        checkIns.value = checkIns.value + newCheckIn
        
        // Still keep streak logic for now but prioritize growth points for status and milestones
        val newStreak = habit.currentStreak + 1
        val updatedHabit = habit.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(habit.longestStreak, newStreak),
            growthPoints = newPoints
        )
        habits.value = habits.value.map { if (it.habitId == habit.habitId) updatedHabit else it }

        // Trigger Milestone if cumulative growth crosses 10, 25, 50, 100
        val milestonesToTrigger = listOf(10, 25, 50, 100)
        if (newPoints in milestonesToTrigger) {
            val milestone = Milestone(
                habitId = habit.habitId,
                habitTitle = habit.title,
                tier = newPoints,
                dateReached = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                encouragementLine = encouragementPhrases.random()
            )
            saveMilestone(milestone)
            return milestone
        }
        return null
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
}
