package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.GrowthStatus
import com.example.ui.components.GrowthBedRow
import com.example.ui.theme.AppIcons

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedOption by remember { mutableStateOf("Voice") }
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    var showPermissionRationale by remember { mutableStateOf(false) }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Proceed to next step regardless of grant state so user is never stuck
        onComplete()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (pagerState.currentPage == 2) {
                Button(
                    onClick = {
                        val sharedPrefs = context.getSharedPreferences("perennial_settings", android.content.Context.MODE_PRIVATE)
                        sharedPrefs.edit().putString("reminder_style", selectedOption).apply()
                        
                        if (selectedOption != "None" && android.os.Build.VERSION.SDK_INT >= 33) {
                            val hasPermission = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (!hasPermission) {
                                showPermissionRationale = true
                            } else {
                                onComplete()
                            }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Moss green
                        contentColor = MaterialTheme.colorScheme.onPrimary // White
                    ),
                ) {
                    Text("Plant your first habit", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                2 -> OnboardingPage3(selectedOption = selectedOption, onOptionSelected = { selectedOption = it })
            }
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { 
                showPermissionRationale = false
                onComplete()
            },
            title = {
                Text(
                    "Enable Gentle Reminders?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "To keep your virtual plants healthy and nurture your daily habits, Perennial can send you silent, warm, and guilt-free nudges. Would you like to enable them?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onComplete()
                        }
                    }
                ) {
                    Text("Yes, enable", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        onComplete()
                    }
                ) {
                    Text("Maybe later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun OnboardingPage1() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Habits\nwithout the\nguilt trip.", 
                 style = MaterialTheme.typography.displayMedium, 
                 textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = AppIcons.Squiggle),
                contentDescription = null,
                modifier = Modifier.width(24.dp).height(8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Missed days don't erase\nyour progress. Your growth\ncontinues, always.", 
                 style = MaterialTheme.typography.bodyLarge, 
                 textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = AppIcons.OnboardingWelcome),
            contentDescription = "Perennial plant growing in soil",
            modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OnboardingPage2() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = AppIcons.OnboardingStreak),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("A missed day\njust rests.\nIt doesn't reset.", 
             style = MaterialTheme.typography.headlineMedium, 
             textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
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
fun OnboardingPage3(selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("How would you like\nyour reminders?", 
             style = MaterialTheme.typography.headlineMedium, 
             textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReminderOptionCard(
                title = "Voice", 
                subtitle = "A gentle nudge to help you remember.", 
                iconRes = AppIcons.GrowthLeaf, 
                selected = selectedOption == "Voice",
                onClick = { onOptionSelected("Voice") }
            )
            ReminderOptionCard(
                title = "Text", 
                subtitle = "A kind text at the right time.", 
                iconRes = AppIcons.HabitRead, 
                selected = selectedOption == "Text",
                onClick = { onOptionSelected("Text") }
            )
            ReminderOptionCard(
                title = "None", 
                subtitle = "I'll check in on my own.", 
                iconRes = AppIcons.GrowthSeed, 
                selected = selectedOption == "None",
                onClick = { onOptionSelected("None") }
            )
        }
    }
}

@Composable
fun ReminderOptionCard(title: String, subtitle: String, iconRes: Int, selected: Boolean, onClick: () -> Unit) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon on the left
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Radio button on the right
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 2.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .background(
                        color = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
