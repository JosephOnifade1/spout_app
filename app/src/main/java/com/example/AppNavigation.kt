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
import com.example.ui.screens.AddEditHabitScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.ShareScreen

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
                onAddHabit = { navController.navigate(Routes.ADD_EDIT_HABIT) },
                onHabitClick = { habitId -> 
                    navController.navigate("habit_detail/$habitId") 
                },
                onCheckIn = { /* TODO */ }
            )
        }
        composable("habit_detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitDetailScreen(
                habit = habits.firstOrNull { it.habitId == habitId } ?: habits[1],
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.ADD_EDIT_HABIT) }
            )
        }
        composable(Routes.ADD_EDIT_HABIT) {
            AddEditHabitScreen(
                habitId = null,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
        composable(Routes.PAYWALL) {
            PaywallScreen(onClose = { navController.popBackStack() })
        }
        composable("share/{habitId}") { backStackEntry ->
            ShareScreen(
                habitTitle = "Read 20 Minutes",
                streak = 30,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
