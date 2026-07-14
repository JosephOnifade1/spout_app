package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.SproutTypography
import com.example.ui.theme.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToNotificationTimes: () -> Unit = {},
    onSignOut: () -> Unit = {},
    themeViewModel: ThemeViewModel,
    subscriptionTier: String = "free",
    onToggleSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    
    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedReminderStyle by remember { mutableStateOf("Voice") }

    val user = remember {
        try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }
    }

    if (showReminderDialog) {
        ReminderStyleDialog(
            currentStyle = selectedReminderStyle,
            onDismiss = { showReminderDialog = false },
            onSelect = { 
                selectedReminderStyle = it
                showReminderDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        style = SproutTypography.HeadlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item { SectionHeader("Preferences") }
            item { 
                SettingsRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Reminder style",
                    subtitle = selectedReminderStyle,
                    onClick = { showReminderDialog = true }
                ) 
            }
            item { 
                SettingsRow(
                    icon = Icons.Outlined.Schedule,
                    title = "Notification times",
                    subtitle = "7:30 AM, 7:30 PM",
                    onClick = onNavigateToNotificationTimes
                ) 
            }
            item { 
                SettingsToggleRow(
                    icon = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    title = "Dark Mode",
                    subtitle = if (isDarkTheme) "Using Ink Teal palette" else "Using Sage palette",
                    checked = isDarkTheme,
                    onCheckedChange = { themeViewModel.setDarkTheme(it) }
                )
            }
            
            item { SectionHeader("Subscription & Data") }
            item { 
                SettingsRow(
                    icon = Icons.Outlined.CreditCard,
                    title = "Subscription management",
                    subtitle = if (subscriptionTier == "plus") "Sprout Plus Plan" else "Free Plan",
                    onClick = onNavigateToPaywall
                ) 
            }
            item {
                SettingsRow(
                    icon = Icons.Outlined.Star,
                    title = "Simulate ${if (subscriptionTier == "free") "Plus" else "Free"}",
                    subtitle = "For testing limit logic",
                    onClick = onToggleSubscription
                )
            }
            item { 
                SettingsRow(
                    icon = Icons.Outlined.FileDownload,
                    title = "Data export",
                    subtitle = "JSON format",
                    onClick = { 
                        coroutineScope.launch {
                            exportData(context)
                        }
                    }
                ) 
            }
            
            item { SectionHeader("Account") }
            item { 
                SettingsRow(
                    icon = Icons.Outlined.Person,
                    title = "Account",
                    subtitle = user?.email ?: "Guest",
                    onClick = onNavigateToAccount
                ) 
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            item {
                SettingsRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Sign out",
                    titleColor = MaterialTheme.colorScheme.error,
                    showChevron = false,
                    onClick = {
                        try {
                            FirebaseAuth.getInstance().signOut()
                        } catch (e: Exception) {}
                        onSignOut()
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
            
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sprout v1.2.0", 
                        style = SproutTypography.BodyMedium, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = SproutTypography.LabelSmallMono,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = SproutTypography.BodyLarge, color = titleColor)
                if (subtitle != null) {
                    Text(
                        subtitle, 
                        style = SproutTypography.BodyMedium, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = SproutTypography.BodyLarge)
                Text(
                    subtitle, 
                    style = SproutTypography.BodyMedium, 
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun ReminderStyleDialog(
    currentStyle: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder Style", style = SproutTypography.TitleMedium) },
        text = {
            Column {
                val options = listOf("Voice", "Text-only", "None")
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentStyle,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, style = SproutTypography.BodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    )
}

private fun exportData(context: Context) {
    val habitsJson = JSONArray().apply {
        put(JSONObject().apply {
            put("habit", "Drink Water")
            put("growth_points", 12)
        })
    }
    
    val exportFile = File(context.cacheDir, "sprout_export.json")
    exportFile.writeText(habitsJson.toString(2))
    
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", exportFile)
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export Data"))
}
