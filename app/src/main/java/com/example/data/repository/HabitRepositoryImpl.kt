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

    override fun getRecentCheckIns(habitId: String): Flow<List<CheckIn>> = checkIns.map { list ->
        list.filter { it.habitId == habitId }.sortedBy { it.date }.takeLast(7)
    }

    override suspend fun checkIn(habit: Habit, date: String, status: GrowthStatus): Milestone? {
        val newCheckIn = CheckIn(
            habitId = habit.habitId,
            userId = "local_user",
            date = date,
            status = status.value
        )
        checkIns.value = checkIns.value + newCheckIn
        
        val newStreak = habit.currentStreak + 1
        val updatedHabit = habit.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(habit.longestStreak, newStreak)
        )
        habits.value = habits.value.map { if (it.habitId == habit.habitId) updatedHabit else it }

        // Trigger Milestone if streak crosses 7, 30, or 100
        if (newStreak == 7 || newStreak == 30 || newStreak == 100) {
            val milestone = Milestone(
                habitId = habit.habitId,
                habitTitle = habit.title,
                tier = newStreak,
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
