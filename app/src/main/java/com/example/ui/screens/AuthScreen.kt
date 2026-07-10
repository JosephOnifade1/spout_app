package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uD83C\uDF31", style = MaterialTheme.typography.displayLarge)
        Text("sprout", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(64.dp))
        
        OutlinedButton(
            onClick = onAuthSuccess,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Continue with Apple", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onAuthSuccess,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Continue with Google", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onAuthSuccess,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Continue with email", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text("Your data stays private and\nsynced across your devices.", 
            style = MaterialTheme.typography.bodySmall, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
