package com.example.repository

import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class HabitRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getHabits(): Flow<List<Habit>> = flow {
        val uid = auth.currentUser?.uid ?: return@flow
        try {
            val snapshot = db.collection("habits")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val habits = snapshot.documents.mapNotNull { it.toObject(Habit::class.java) }
            emit(habits)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getRecentCheckIns(habitId: String): Flow<List<CheckIn>> = flow {
        try {
            val snapshot = db.collection("check_ins")
                .whereEqualTo("habitId", habitId)
                .orderBy("date")
                .limitToLast(7)
                .get()
                .await()
            val checkIns = snapshot.documents.mapNotNull { it.toObject(CheckIn::class.java) }
            emit(checkIns)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun checkIn(habit: Habit, date: String, status: GrowthStatus) {
        val uid = auth.currentUser?.uid ?: return
        val checkIn = CheckIn(
            habitId = habit.habitId,
            userId = uid,
            date = date,
            status = status.value
        )
        try {
            db.collection("check_ins").document(checkIn.checkInId).set(checkIn).await()
            // In a real app we would compute the new streak here and update the habit doc
            val updatedHabit = habit.copy(
                currentStreak = habit.currentStreak + 1,
                longestStreak = maxOf(habit.longestStreak, habit.currentStreak + 1)
            )
            db.collection("habits").document(habit.habitId).set(updatedHabit).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveHabit(habit: Habit) {
        val uid = auth.currentUser?.uid ?: return
        val newHabit = habit.copy(userId = uid)
        try {
            db.collection("habits").document(newHabit.habitId).set(newHabit).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
