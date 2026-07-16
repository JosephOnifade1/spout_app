package com.example.domain.repository

import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.data.Milestone
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getHabits(): Flow<List<Habit>>
    fun getRecentCheckIns(habitId: String): Flow<List<CheckIn>>
    suspend fun saveHabit(habit: Habit)
    suspend fun initializeDefaultHabits()
    suspend fun updateHabitOrder(habitIds: List<String>)
    fun getWeeklyCheckIns(days: Int = 7): Flow<List<CheckIn>>
    
    fun getMilestones(): Flow<List<Milestone>>
    suspend fun saveMilestone(milestone: Milestone)

    fun getSubscriptionTier(): Flow<String>
    suspend fun updateSubscriptionTier(tier: String)
    fun canAddHabit(): Flow<Boolean>

    suspend fun toggleCheckIn(habitId: String): Milestone?
    suspend fun deleteHabitPermanently(habitId: String)
}
