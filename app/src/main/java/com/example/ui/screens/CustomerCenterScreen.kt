package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter

@Composable
fun CustomerCenterScreen(onBack: () -> Unit) {
    CustomerCenter(
        onDismiss = onBack
    )
}
