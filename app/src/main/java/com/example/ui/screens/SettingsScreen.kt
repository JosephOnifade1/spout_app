package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SettingsItem(title = "Reminder style", subtitle = "Voice", onClick = {}) }
            item { SettingsItem(title = "Notification times", subtitle = "7:30 AM, 7:30 PM", onClick = {}) }
            item { SettingsItem(title = "Theme", subtitle = "Light (Sage)", onClick = {}) }
            item { SettingsItem(title = "Data export", onClick = {}) }
            item { SettingsItem(title = "Subscription management", onClick = {}) }
            item { SettingsItem(title = "Account", onClick = {}) }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("← ", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Sign out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
            
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sprout v1.2.0", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
            }
        }
        Text("→", color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f), style = MaterialTheme.typography.bodyLarge)
    }
}
