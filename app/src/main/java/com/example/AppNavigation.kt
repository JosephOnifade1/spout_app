package com.example

import androidx.compose.runtime.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.currentBackStackEntryAsState
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
fun PerennialApp(themeViewModel: ThemeViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val database = remember { com.example.data.local.PerennialDatabase.getDatabase(context) }
    val repository = remember { HabitRepositoryImpl(database.habitDao(), context.applicationContext) }
    val habits by repository.getHabits().collectAsState(initial = emptyList())
    val allCheckIns by repository.getWeeklyCheckIns().collectAsState(initial = emptyList())
    val canAddHabit by repository.canAddHabit().collectAsState(initial = true)
    val subscriptionTier by repository.getSubscriptionTier().collectAsState(initial = "free")
    val coroutineScope = rememberCoroutineScope()
    var milestoneToCelebrate by remember { mutableStateOf<Milestone?>(null) }
    var paywallMessage by remember { mutableStateOf<String?>(null) }

    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val userName by repository.getUserDisplayName().collectAsState(initial = "Friend")

    LaunchedEffect(currentUser) {
        repository.onUserChanged(currentUser?.uid)
        if (currentUser == null) {
            // If user is logged out, and not on onboarding, send to auth
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Routes.AUTH && currentRoute != Routes.ONBOARDING) {
                navController.navigate(Routes.AUTH) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // User just logged in, if they were on Auth, send them to Today
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == Routes.AUTH) {
                coroutineScope.launch {
                    repository.initializeDefaultHabits()
                    repository.syncSubscriptionStatus()
                    navController.navigate(Routes.TODAY) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            } else {
                coroutineScope.launch {
                    repository.initializeDefaultHabits()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        repository.syncSubscriptionStatus()
    }

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

    val startRoute = remember {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
            Routes.TODAY
        } else {
            Routes.ONBOARDING
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.TODAY,
        Routes.GARDEN,
        Routes.MILESTONES,
        Routes.SETTINGS
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                com.example.ui.components.BottomNavBar(
                    currentRoute = currentRoute ?: Routes.TODAY,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Routes.TODAY) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onComplete = { navController.navigate(Routes.AUTH) })
        }
        composable(Routes.AUTH) {
            AuthScreen(onAuthSuccess = { 
                // Navigation handled by AuthStateListener
            })
        }
        composable(Routes.TODAY) {
            TodayScreen(
                userName = userName,
                habits = habits,
                allCheckIns = allCheckIns,
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
                        val milestone = repository.toggleCheckIn(habit.habitId)
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
                initialHabit = null,
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
            val habit = habits.find { it.habitId == habitId }
            AddEditHabitScreen(
                initialHabit = habit,
                onSave = { updatedHabit -> 
                    coroutineScope.launch { 
                        repository.saveHabit(updatedHabit)
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
                habits = habits,
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                onNavigateToAccount = { navController.navigate(Routes.ACCOUNT) },
                onNavigateToNotificationTimes = { navController.navigate(Routes.NOTIFICATION_TIMES) },
                onNavigateToArchivedHabits = { navController.navigate(Routes.ARCHIVED_HABITS) },
                onNavigateToCustomerCenter = { navController.navigate(Routes.CUSTOMER_CENTER) },
                onSignOut = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
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
        composable(Routes.ARCHIVED_HABITS) {
            ArchivedHabitsScreen(
                habits = habits,
                onBack = { navController.popBackStack() },
                onRestore = { habit ->
                    if (canAddHabit) {
                        coroutineScope.launch {
                            repository.saveHabit(habit.copy(archived = false))
                        }
                    } else {
                        paywallMessage = "You've already got 3 active habits. Upgrade to grow more, or archive one first."
                        navController.navigate(Routes.PAYWALL)
                    }
                },
                onDeletePermanently = { habit ->
                    coroutineScope.launch {
                        repository.deleteHabitPermanently(habit.habitId)
                    }
                }
            )
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
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
                repository = repository,
                onClose = { 
                    paywallMessage = null
                    navController.popBackStack() 
                }
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
        composable(Routes.CUSTOMER_CENTER) {
            CustomerCenterScreen(onBack = { navController.popBackStack() })
        }
    }
    }
}
