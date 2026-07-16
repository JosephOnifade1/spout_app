package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val FrauncesFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = provider)
)

val PublicSansFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Public Sans"), fontProvider = provider)
)

val IbmPlexMonoFontFamily = FontFamily(
    Font(googleFont = GoogleFont("IBM Plex Mono"), fontProvider = provider)
)

/**
 * PerennialTypography defines the core brand styles for the Perennial application.
 * It uses Fraunces for display and headings, and IBM Plex Mono for technical/numeric data.
 */
object PerennialTypography {
    val HeadlineLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )
    
    val HeadlineMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
    
    val HeadlineSmall = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
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
        letterSpacing = 0.5.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    val TechnicalMono = TextStyle(
        fontFamily = IbmPlexMonoFontFamily,
        fontWeight = FontWeight.Bold,
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
        letterSpacing = 0.5.sp
    ),
    labelSmall = PerennialTypography.LabelSmallMono
)
