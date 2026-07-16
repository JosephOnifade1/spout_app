package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppIcons
import com.example.ui.theme.Butter
import com.example.ui.theme.PerennialTypography

@Composable
fun MilestoneModal(
    streak: Int,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Butter)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Decorative elements (low opacity leaves/sparkles)
                DecorativeElements()

                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 48.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Amazing!",
                        style = PerennialTypography.LabelSmallMono.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC97B84) // Dusty Clay accent
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "You've nurtured this for",
                        style = PerennialTypography.BodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        streak.toString(),
                        style = PerennialTypography.HeadlineLarge.copy(fontSize = 72.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "growth points",
                        style = PerennialTypography.BodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Bloom illustration in soil mound
                    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.size(120.dp)) {
                        // Soil mound (simplified)
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(20.dp)
                                .background(Color(0xFF8C7A6B), RoundedCornerShape(10.dp))
                        )
                        Image(
                            painter = painterResource(id = AppIcons.GrowthBloom),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .offset(y = (-10).dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = onShare,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B7F5E))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Share this milestone", style = PerennialTypography.BodyLarge.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Text("Keep going", style = PerennialTypography.BodyLarge, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun DecorativeElements() {
    // Simple low-opacity leaves/sparkles scattered
    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        // Just a few icons for visual flair
        Icon(
            painter = painterResource(id = AppIcons.PerennialOutline),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .offset(x = 40.dp, y = 60.dp)
                .alpha(0.1f),
            tint = Color.DarkGray
        )
        Icon(
            painter = painterResource(id = AppIcons.PerennialOutline),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .offset(x = 280.dp, y = 120.dp)
                .alpha(0.1f),
            tint = Color.DarkGray
        )
    }
}
