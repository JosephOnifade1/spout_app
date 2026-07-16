package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.HabitRepository
import com.example.ui.theme.Moss
import com.example.ui.theme.PaperSage
import com.example.ui.theme.PerennialTypography
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    repository: HabitRepository,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var offerings by remember { mutableStateOf<com.revenuecat.purchases.Offerings?>(null) }
    var selectedPackageType by remember { mutableStateOf(PackageType.ANNUAL) }
    var isLoading by remember { mutableStateOf(true) }
    var purchaseInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        offerings = repository.getOfferings()
        isLoading = false
    }

    val currentOffering = offerings?.current
    val monthlyPackage = currentOffering?.availablePackages?.find { it.packageType == PackageType.MONTHLY }
    val yearlyPackage = currentOffering?.availablePackages?.find { it.packageType == PackageType.ANNUAL }
    
    val selectedPackage = if (selectedPackageType == PackageType.ANNUAL) yearlyPackage else monthlyPackage
    val priceText = selectedPackage?.product?.price?.formatted ?: if (selectedPackageType == PackageType.ANNUAL) "$79.99/yr" else "$9.99/mo"

    Scaffold(
        containerColor = PaperSage,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Grow more habits",
                style = PerennialTypography.HeadlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "More space. More support.\nMore ways to grow.",
                style = PerennialTypography.BodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Segmented Toggle
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selectedPackageType == PackageType.MONTHLY) Color.White else Color.Transparent)
                        .clickable { selectedPackageType = PackageType.MONTHLY }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Monthly",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedPackageType == PackageType.MONTHLY) Moss else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selectedPackageType == PackageType.ANNUAL) Color.White else Color.Transparent)
                        .clickable { selectedPackageType = PackageType.ANNUAL }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Yearly",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedPackageType == PackageType.ANNUAL) Moss else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Moss.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                "SAVE 33%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Moss,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Comparison Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlanCard(
                    title = "Free",
                    subtitle = "3 habits",
                    features = listOf("Basic reminders", "Light theme"),
                    isSelected = false,
                    modifier = Modifier.weight(1f)
                )
                PlanCard(
                    title = "Plus",
                    subtitle = "Unlimited",
                    features = listOf("Reminder voices", "Data export", "All themes", "Priority support"),
                    isSelected = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // CTA Button
            Button(
                onClick = {
                    selectedPackage?.let { pkg ->
                        coroutineScope.launch {
                            purchaseInProgress = true
                            val result = repository.purchasePackage(context as Activity, pkg)
                            purchaseInProgress = false
                            if (result.isSuccess) {
                                onClose()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Moss,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && !purchaseInProgress && selectedPackage != null
            ) {
                if (purchaseInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        "Start growing — $priceText",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Cancel anytime. No guilt if you do.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    subtitle: String,
    features: List<String>,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White else Color.Transparent
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Moss else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                title,
                style = PerennialTypography.TitleMedium,
                color = if (isSelected) Moss else MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                style = PerennialTypography.BodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("•", color = if (isSelected) Moss else Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                if (isSelected) "🌿" else "🌱",
                style = TextStyle(fontSize = 24.sp),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
