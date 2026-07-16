package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Moss, 
    secondary = DustyClay, 
    tertiary = Butter,
    background = InkTeal,
    surface = CharcoalInk,
    onPrimary = PaperSage,
    onBackground = PaperSage,
    onSurface = PaperSage,
    error = Soil
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Moss,
    secondary = DustyClay,
    tertiary = Butter,
    background = PaperSage,
    surface = White,
    onPrimary = White,
    onBackground = CharcoalInk,
    onSurface = CharcoalInk,
    error = Soil
  )

@Composable
fun PerennialTheme(
  darkTheme: Boolean = false,
  // Disabling dynamic color to enforce specific brand tokens
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
