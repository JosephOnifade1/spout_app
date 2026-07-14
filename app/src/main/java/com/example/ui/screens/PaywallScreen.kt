package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(onClose: () -> Unit, message: String? = null) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onClose) {
                        Text("✕", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (message != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text("Grow more habits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(16.dp))
            Text("More space. More support.\nMore ways to grow.", 
                 style = MaterialTheme.typography.bodyLarge, 
                 textAlign = TextAlign.Center,
                 color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.8f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PlanCard(
                    title = "Free",
                    subtitle = "3 habits",
                    features = listOf("Basic reminders", "Light theme"),
                    modifier = Modifier.weight(1f),
                    selected = false
                )
                PlanCard(
                    title = "Sprout Plus",
                    subtitle = "Unlimited habits",
                    features = listOf("Reminder voices", "Data export", "All themes", "Priority support"),
                    modifier = Modifier.weight(1f),
                    selected = true
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Start growing — $5/mo.")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cancel anytime. No guilt if you do.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlanCard(title: String, subtitle: String, features: List<String>, modifier: Modifier = Modifier, selected: Boolean) {
    Card(
        modifier = modifier.then(
            if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            else Modifier
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
            Spacer(modifier = Modifier.height(24.dp))
            features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Text("✓ ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(feature, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(if (selected) "\uD83C\uDF3F" else "\uD83C\uDF31", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
