package com.example

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.Habit
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.Routes
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.AuthScreen

@Composable
fun SproutApp() {
    val navController = rememberNavController()
    
    // Mock data for UI
    val habits = listOf(
        Habit(title = "Drink Water", currentStreak = 12, longestStreak = 38),
        Habit(title = "Read 20 Minutes", currentStreak = 5, longestStreak = 14),
        Habit(title = "Meditate", currentStreak = 0, longestStreak = 7)
    )

    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onComplete = { navController.navigate(Routes.AUTH) })
        }
        composable(Routes.AUTH) {
            AuthScreen(onAuthSuccess = { 
                navController.navigate(Routes.TODAY) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.TODAY) {
            TodayScreen(
                habits = habits,
                onAddHabit = { /* TODO */ },
                onHabitClick = { habitId -> 
                    navController.navigate("habit_detail/$habitId") 
                },
                onCheckIn = { /* TODO */ }
            )
        }
        composable("habit_detail/{habitId}") { backStackEntry ->
            // In a real app, use the ID to get the habit
            HabitDetailScreen(
                habit = habits[1],
                onBack = { navController.popBackStack() },
                onEdit = { /* TODO */ }
            )
        }
    }
}
