package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.GrowthBedRow
import com.example.data.GrowthStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Scaffold(
        bottomBar = {
            if (pagerState.currentPage == 2) {
                PaddingValues(16.dp)
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
                ) {
                    Text("Plant your first habit", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            when (page) {
                0 -> OnboardingPage1()
                1 -> OnboardingPage2()
                2 -> OnboardingPage3()
            }
        }
    }
}

@Composable
fun OnboardingPage1() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Habits\nwithout the\nguilt trip.", 
            style = MaterialTheme.typography.displayMedium, 
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Missed days don't erase your progress. Your growth continues, always.", 
            style = MaterialTheme.typography.bodyLarge, 
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OnboardingPage2() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("A missed day\njust rests.\nIt doesn't reset.", 
            style = MaterialTheme.typography.headlineMedium, 
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        GrowthBedRow(recentStatuses = GrowthStatus.values().toList())
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            GrowthStatus.values().forEach {
                Text(it.value.replaceFirstChar { c -> c.uppercase() }, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun OnboardingPage3() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("How would you like\nyour reminders?", 
            style = MaterialTheme.typography.headlineMedium, 
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        ReminderOptionCard("Voice", "A gentle nudge to help you remember.", true)
        Spacer(modifier = Modifier.height(16.dp))
        ReminderOptionCard("Text", "A kind text at the right time.", false)
        Spacer(modifier = Modifier.height(16.dp))
        ReminderOptionCard("None", "I'll check in on my own.", false)
    }
}

@Composable
fun ReminderOptionCard(title: String, subtitle: String, selected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
