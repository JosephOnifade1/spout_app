package com.example.data

import java.util.UUID

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val reminderStyle: String = "Voice",
    val theme: String = "Light (Sage)",
    val subscriptionTier: String = "Free",
    val createdAt: Long = System.currentTimeMillis()
)

data class Habit(
    val habitId: String = UUID.randomUUID().toString(),
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

data class Milestone(
    val milestoneId: String = UUID.randomUUID().toString(),
    val habitId: String = "",
    val userId: String = "local_user",
    val habitTitle: String = "",
    val tier: Int = 0, // 7, 30, 100
    val dateReached: String = "",
    val encouragementLine: String = ""
)

data class CheckIn(
    val checkInId: String = UUID.randomUUID().toString(),
    val habitId: String = "",
    val userId: String = "",
    val date: String = "", // e.g. "2026-07-11"
    val status: String = "seed", // "bloom"/"bud"/"sprout"/"seed"/"resting"
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class GrowthStatus(val value: String) {
    SEED("seed"),
    SPROUT("sprout"),
    BUD("bud"),
    BLOOM("bloom"),
    RESTING("resting")
}
