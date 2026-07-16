package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun signInAnonymously() {
        isLoading = true
        coroutineScope.launch {
            delay(1000) // Mock network request
            isLoading = false
            onAuthSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.5f)) // Generous top padding ~1/3

            Image(
                painter = painterResource(id = AppIcons.PerennialOutline),
                contentDescription = "Perennial icon",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "perennial", 
                style = MaterialTheme.typography.displayMedium, 
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary // moss/ink teal tone
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { signInAnonymously() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface, // Paper sage / white
                        contentColor = MaterialTheme.colorScheme.onBackground // Charcoal text
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = AppIcons.AppleLogo),
                            contentDescription = "Apple",
                            modifier = Modifier.size(20.dp).align(Alignment.CenterStart)
                        )
                        Text("Continue with Apple", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Center))
                    }
                }

                OutlinedButton(
                    onClick = { signInAnonymously() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface, // Paper sage / white
                        contentColor = MaterialTheme.colorScheme.onBackground // Charcoal text
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = AppIcons.GoogleLogo),
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp).align(Alignment.CenterStart)
                        )
                        Text("Continue with Google", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Button(
                    onClick = { signInAnonymously() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Moss green
                        contentColor = MaterialTheme.colorScheme.onPrimary // White
                    ),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary, 
                                modifier = Modifier.size(20.dp).align(Alignment.Center)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = AppIcons.Email),
                                contentDescription = "Email",
                                modifier = Modifier.size(20.dp).align(Alignment.CenterStart)
                            )
                            Text("Continue with email", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Image(
                painter = painterResource(id = AppIcons.Squiggle),
                contentDescription = null,
                modifier = Modifier.width(24.dp).height(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Your data stays private and\nsynced across your devices.", 
                style = MaterialTheme.typography.bodySmall, 
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) // Muted charcoal/soil tone
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
