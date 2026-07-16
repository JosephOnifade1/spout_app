package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val reminderStyle: String = "Voice",
    val theme: String = "Light (Sage)",
    val subscriptionTier: String = "Free",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val habitId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val targetText: String? = null,
    val iconKey: String = "water",
    val colorTag: String = "moss",
    val frequency: String = "Daily",
    val customDays: List<Int> = emptyList(), // 1 for Monday, etc.
    val reminderTime: String = "07:30",
    val reminderEnabled: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val growthPoints: Int = 0,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey val milestoneId: String = UUID.randomUUID().toString(),
    val habitId: String = "",
    val userId: String = "local_user",
    val habitTitle: String = "",
    val tier: Int = 0, // 7, 30, 100
    val dateReached: String = "",
    val encouragementLine: String = ""
)

@Entity(tableName = "check_ins")
data class CheckIn(
    @PrimaryKey val checkInId: String = UUID.randomUUID().toString(),
    val habitId: String = "",
    val userId: String = "",
    val date: String = "", // e.g. "2026-07-11"
    val status: String = "seed", // "bloom"/"bud"/"perennial"/"seed"/"resting"
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class Converters {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}

enum class GrowthStatus(val value: String) {
    SEED("seed"),
    SPROUT("perennial"),
    BUD("bud"),
    BLOOM("bloom"),
    RESTING("resting"),
    UNSCHEDULED("unscheduled"),
    BLANK("blank")
}
