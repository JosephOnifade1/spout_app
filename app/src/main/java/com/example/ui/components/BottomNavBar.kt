package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppIcons

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "today",
            onClick = { onNavigate("today") },
            icon = { 
                Icon(
                    painterResource(id = if (currentRoute == "today") AppIcons.GrowthLeafFilled else AppIcons.GrowthLeaf), 
                    contentDescription = "Today", 
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { 
                Text(
                    "Today", 
                    fontWeight = if (currentRoute == "today") FontWeight.Bold else FontWeight.Normal,
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha=0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "garden",
            onClick = { onNavigate("garden") },
            icon = { 
                Icon(
                    painterResource(id = if (currentRoute == "garden") AppIcons.GrowthSeedFilled else AppIcons.GrowthSeed), 
                    contentDescription = "Garden", 
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { 
                Text(
                    "Garden",
                    fontWeight = if (currentRoute == "garden") FontWeight.Bold else FontWeight.Normal,
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha=0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "milestones",
            onClick = { onNavigate("milestones") },
            icon = { 
                Icon(
                    painterResource(id = if (currentRoute == "milestones") AppIcons.GrowthBloomFilled else AppIcons.GrowthBloom), 
                    contentDescription = "Milestones", 
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { 
                Text(
                    "Milestones",
                    fontWeight = if (currentRoute == "milestones") FontWeight.Bold else FontWeight.Normal,
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha=0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = { 
                Icon(
                    painterResource(id = if (currentRoute == "settings") AppIcons.NavYouFilled else AppIcons.NavYou), 
                    contentDescription = "You", 
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { 
                Text(
                    "You",
                    fontWeight = if (currentRoute == "settings") FontWeight.Bold else FontWeight.Normal,
                ) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha=0.15f),
                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
            )
        )
    }
}
