package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Fonts are bundled in res/font (latin subset, ~242 KB total) instead of being pulled at
 * runtime from the Google Fonts provider. The previous implementation depended on Google
 * Play Services being present and on a network round-trip, so on a fresh install — or
 * offline — every heading silently fell back to the system sans-serif.
 */
val FrauncesFontFamily = FontFamily(
    Font(R.font.fraunces_400, FontWeight.Normal),
    Font(R.font.fraunces_600, FontWeight.SemiBold),
    Font(R.font.fraunces_700, FontWeight.Bold)
)

val PublicSansFontFamily = FontFamily(
    Font(R.font.public_sans_400, FontWeight.Normal),
    Font(R.font.public_sans_500, FontWeight.Medium),
    Font(R.font.public_sans_600, FontWeight.SemiBold),
    Font(R.font.public_sans_700, FontWeight.Bold)
)

val IbmPlexMonoFontFamily = FontFamily(
    Font(R.font.ibm_plex_mono_400, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_500, FontWeight.Medium)
)

/**
 * PerennialTypography defines the core brand styles for the Perennial application.
 * It uses Fraunces for display and headings, and IBM Plex Mono for technical/numeric data.
 *
 * Display styles use SemiBold (600) rather than Bold: at 24-32sp a high-contrast serif
 * reads as shouting at 700. A true 700 cut is still bundled so existing call sites that
 * ask for FontWeight.Bold get real bold instead of a synthesised smear.
 * Letter spacing is slightly negative on display sizes to close up Fraunces' sidebearings.
 */
object PerennialTypography {
    val HeadlineLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.4).sp
    )

    val HeadlineMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    )

    val HeadlineSmall = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    )

    val TitleLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    val TitleMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

    val BodyLarge = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    )

    val TechnicalMono = TextStyle(
        fontFamily = IbmPlexMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

    val LabelSmallMono = TextStyle(
        fontFamily = IbmPlexMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
}

val AppTypography = Typography(
    headlineLarge = PerennialTypography.HeadlineLarge,
    headlineMedium = PerennialTypography.HeadlineMedium,
    headlineSmall = PerennialTypography.HeadlineSmall,
    titleLarge = PerennialTypography.TitleLarge,
    titleMedium = PerennialTypography.TitleMedium,
    bodyLarge = PerennialTypography.BodyLarge,
    bodyMedium = PerennialTypography.BodyMedium,
    bodySmall = PerennialTypography.LabelSmallMono,
    labelLarge = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = PerennialTypography.LabelSmallMono
)
