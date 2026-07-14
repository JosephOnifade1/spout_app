package com.example

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.data.Milestone
import com.example.data.repository.HabitRepositoryImpl
import com.example.ui.screens.*
import com.example.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun SproutApp(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val repository = remember { HabitRepositoryImpl() }
    val habits by repository.getHabits().collectAsState(initial = emptyList())
    val allCheckIns by repository.getWeeklyCheckIns().collectAsState(initial = emptyList())
    val canAddHabit by repository.canAddHabit().collectAsState(initial = true)
    val subscriptionTier by repository.getSubscriptionTier().collectAsState(initial = "free")
    val coroutineScope = rememberCoroutineScope()
    var milestoneToCelebrate by remember { mutableStateOf<Milestone?>(null) }
    var paywallMessage by remember { mutableStateOf<String?>(null) }

    milestoneToCelebrate?.let { milestone ->
        MilestoneModal(
            streak = milestone.tier,
            onShare = { 
                navController.navigate("share/${milestone.habitId}")
                milestoneToCelebrate = null
            },
            onDismiss = { milestoneToCelebrate = null }
        )
    }

    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onComplete = { navController.navigate(Routes.AUTH) })
        }
        composable(Routes.AUTH) {
            AuthScreen(onAuthSuccess = { coroutineScope.launch { repository.initializeDefaultHabits();  
                 navController.navigate(Routes.TODAY) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true } }
                }
            })
        }
        composable(Routes.TODAY) {
            TodayScreen(
                habits = habits,
                onAddHabit = { 
                    if (canAddHabit) {
                        navController.navigate(Routes.ADD_EDIT_HABIT)
                    } else {
                        paywallMessage = "You've planted 3 habits — upgrade to plant more"
                        navController.navigate(Routes.PAYWALL)
                    }
                },
                onHabitClick = { habitId -> 
                     navController.navigate("habit_detail/$habitId") 
                 },
                onCheckIn = { habit -> 
                    coroutineScope.launch {
                        val milestone = repository.checkIn(
                            habit = habit,
                            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            status = GrowthStatus.SEED
                        )
                        if (milestone != null) {
                            milestoneToCelebrate = milestone
                        }
                    }
                },
                onNotificationsClick = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateToTab = { tab ->
                    when (tab) {
                        "settings" -> navController.navigate(Routes.SETTINGS)
                        "garden" -> navController.navigate(Routes.GARDEN)
                        "milestones" -> navController.navigate(Routes.MILESTONES)
                        "today" -> { /* Already here */ }
                    }
                }
            )
        }
        composable(Routes.HABIT_DETAIL) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            val habit = habits.firstOrNull { it.habitId == habitId }
            
            if (habit != null) {
                // Collect recent check-ins for this habit
                val recentCheckIns by repository.getRecentCheckIns(habit.habitId).collectAsState(initial = emptyList())
                
                HabitDetailScreen(
                    habit = habit,
                    recentCheckIns = recentCheckIns,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("edit_habit/${habit.habitId}") },
                    onArchive = { h ->
                        coroutineScope.launch {
                            repository.saveHabit(h.copy(archived = true))
                            navController.popBackStack()
                        }
                    },
                    onUnarchive = { h ->
                        if (canAddHabit) {
                            coroutineScope.launch {
                                repository.saveHabit(h.copy(archived = false))
                                navController.popBackStack()
                            }
                        } else {
                            paywallMessage = "You've already got 3 active habits. Upgrade to grow more, or archive one first."
                            navController.navigate(Routes.PAYWALL)
                        }
                    }
                )
            }
        }
        composable(Routes.ADD_EDIT_HABIT) {
            AddEditHabitScreen(
                habitId = null,
                onSave = { habit -> 
                    coroutineScope.launch { 
                        repository.saveHabit(habit)
                        navController.popBackStack() 
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EDIT_HABIT) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            AddEditHabitScreen(
                habitId = habitId,
                onSave = { habit -> 
                    coroutineScope.launch { 
                        repository.saveHabit(habit.copy(habitId = habitId ?: UUID.randomUUID().toString()))
                        navController.popBackStack() 
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.GARDEN) {
            GardenScreen(
                habits = habits,
                allCheckIns = allCheckIns,
                canAddHabit = canAddHabit,
                subscriptionTier = subscriptionTier,
                onAddHabit = {
                    if (canAddHabit) {
                        navController.navigate(Routes.ADD_EDIT_HABIT)
                    } else {
                        paywallMessage = "You've planted 3 habits — upgrade to plant more"
                        navController.navigate(Routes.PAYWALL)
                    }
                },
                onHabitClick = { habitId ->
                    navController.navigate("habit_detail/$habitId")
                },
                onReorderHabits = { habitIds ->
                    coroutineScope.launch {
                        repository.updateHabitOrder(habitIds)
                    }
                },
                onNavigateToTab = { tab ->
                    when (tab) {
                        "settings" -> navController.navigate(Routes.SETTINGS)
                        "today" -> navController.navigate(Routes.TODAY) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                        "milestones" -> navController.navigate(Routes.MILESTONES)
                        "garden" -> { /* Already here */ }
                    }
                }
            )
        }

        composable(Routes.MILESTONES) {
            val milestones by repository.getMilestones().collectAsState(initial = emptyList())
            MilestonesScreen(
                milestones = milestones,
                habits = habits,
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onNavigateToTab = { tab ->
                    when (tab) {
                        "settings" -> navController.navigate(Routes.SETTINGS)
                        "today" -> navController.navigate(Routes.TODAY) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                        "garden" -> navController.navigate(Routes.GARDEN)
                        "milestones" -> { /* Already here */ }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                onNavigateToAccount = { navController.navigate(Routes.ACCOUNT) },
                onNavigateToNotificationTimes = { navController.navigate(Routes.NOTIFICATION_TIMES) },
                onSignOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                themeViewModel = themeViewModel,
                subscriptionTier = subscriptionTier,
                onToggleSubscription = {
                    coroutineScope.launch {
                        repository.updateSubscriptionTier(if (subscriptionTier == "free") "plus" else "free")
                    }
                }
            )
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.NOTIFICATION_TIMES) {
            NotificationTimesScreen(
                habits = habits,
                onBack = { navController.popBackStack() },
                onUpdateTime = { habit, time ->
                    coroutineScope.launch {
                        repository.saveHabit(habit.copy(reminderTime = time))
                    }
                }
            )
        }
        composable(Routes.PAYWALL) {
            PaywallScreen(
                onClose = { 
                    paywallMessage = null
                    navController.popBackStack() 
                },
                message = paywallMessage
            )
        }
        composable(Routes.SHARE) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            val habit = habits.firstOrNull { it.habitId == habitId }
            if (habit != null) {
                ShareScreen(
                    habitTitle = habit.title,
                    streak = habit.growthPoints,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
    }
}
