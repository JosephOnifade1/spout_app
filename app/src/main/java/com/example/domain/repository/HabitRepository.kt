package com.example.domain.repository

import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getHabits(): Flow<List<Habit>>
    fun getRecentCheckIns(habitId: String): Flow<List<CheckIn>>
    suspend fun checkIn(habit: Habit, date: String, status: GrowthStatus)
    suspend fun saveHabit(habit: Habit)
}
