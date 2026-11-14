package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Light surfaces are warm paper, not white. White next to the hand-painted cream paper of the
 * plant art makes the illustration look pasted on; PaperCream lets it sit inside the surface.
 */
private val LightColorScheme =
  lightColorScheme(
    primary = Moss,
    onPrimary = White,
    primaryContainer = LeafLight,
    onPrimaryContainer = InkTeal,
    secondary = DustyClay,
    onSecondary = White,
    secondaryContainer = BloomPink,
    onSecondaryContainer = InkTeal,
    tertiary = Butter,
    onTertiary = InkTeal,
    background = PaperSage,
    onBackground = InkTeal,
    surface = PaperCream,
    onSurface = CharcoalInk,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = InkSoft,
    outline = Soil,
    outlineVariant = CreamWarm,
    error = DustyClay,
    onError = White
  )

/**
 * Night mode is a real palette, not an inverted one: the art set is generated for it (Phase 6),
 * so the chrome has to match that paper rather than dim the daytime cream into grey mud.
 */
private val DarkColorScheme =
  darkColorScheme(
    primary = LeafLight,
    onPrimary = InkTeal,
    primaryContainer = LeafDeep,
    onPrimaryContainer = PaperSage,
    secondary = DustyClay,
    onSecondary = InkTeal,
    secondaryContainer = SoilDeep,
    onSecondaryContainer = PaperSage,
    tertiary = Butter,
    onTertiary = NightPaper,
    background = NightPaper,
    onBackground = PaperSage,
    surface = NightSurface,
    onSurface = PaperSage,
    surfaceVariant = SoilDeep,
    onSurfaceVariant = InkMuted,
    outline = Soil,
    outlineVariant = CharcoalInk,
    error = DustyClay,
    onError = NightPaper
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

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = PerennialShapes,
    content = content
  )
}
