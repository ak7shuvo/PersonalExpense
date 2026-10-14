package com.ak7shuvo.personalexpense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Semantic colours beyond Material's scheme: money in, money out, money lent, and the dark hero card. */
@Immutable
class FinanceColors(
    val income: Color,
    val expense: Color,
    val loan: Color,
    val hero: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    val heroIncome: Color,
    val heroExpense: Color,
    val chartPalette: List<Color>,
)

private val LightFinance = FinanceColors(
    income = Color(0xFF0E7A5C),
    expense = Color(0xFFB3402F),
    loan = Color(0xFF9A7424),
    hero = Color(0xFF1F1E1C),
    onHero = Color(0xFFF4F0E8),
    onHeroMuted = Color(0xFFA9A397),
    heroIncome = Color(0xFF58D3A8),
    heroExpense = Color(0xFFF0897A),
    chartPalette = listOf(
        Color(0xFFB3402F), Color(0xFFC9803A), Color(0xFF8C7B64),
        Color(0xFF5E7D73), Color(0xFF9B5B6E), Color(0xFFB8B0A1),
    ),
)

private val DarkFinance = FinanceColors(
    income = Color(0xFF4CC9A0),
    expense = Color(0xFFE97B6D),
    loan = Color(0xFFD4B26A),
    hero = Color(0xFF262421),
    onHero = Color(0xFFF4F0E8),
    onHeroMuted = Color(0xFF9E988C),
    heroIncome = Color(0xFF4CC9A0),
    heroExpense = Color(0xFFE97B6D),
    chartPalette = listOf(
        Color(0xFFE97B6D), Color(0xFFDDA05E), Color(0xFFB09C80),
        Color(0xFF7FA597), Color(0xFFC287A0), Color(0xFF6F695F),
    ),
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinance }

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0E7A5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7EFE5),
    onPrimaryContainer = Color(0xFF053D2E),
    secondary = Color(0xFF55524B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEAE5DA),
    onSecondaryContainer = Color(0xFF1F1E1C),
    background = Color(0xFFF7F4EE),
    onBackground = Color(0xFF1F1E1C),
    surface = Color(0xFFFFFDF9),
    onSurface = Color(0xFF1F1E1C),
    surfaceVariant = Color(0xFFEFEBE2),
    onSurfaceVariant = Color(0xFF6B675F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF8F2),
    surfaceContainer = Color(0xFFF3EFE6),
    surfaceContainerHigh = Color(0xFFEDE8DD),
    surfaceContainerHighest = Color(0xFFE6E0D3),
    outline = Color(0xFFB9B2A3),
    outlineVariant = Color(0xFFE2DCCF),
    error = Color(0xFFB3402F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF7DDD7),
    onErrorContainer = Color(0xFF5C150B),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF4CC9A0),
    onPrimary = Color(0xFF06261C),
    primaryContainer = Color(0xFF14463A),
    onPrimaryContainer = Color(0xFFBDEFDD),
    secondary = Color(0xFFC3BDB0),
    onSecondary = Color(0xFF2A2823),
    secondaryContainer = Color(0xFF34312C),
    onSecondaryContainer = Color(0xFFEDEAE3),
    background = Color(0xFF131312),
    onBackground = Color(0xFFEDEAE3),
    surface = Color(0xFF1C1B19),
    onSurface = Color(0xFFEDEAE3),
    surfaceVariant = Color(0xFF2A2825),
    onSurfaceVariant = Color(0xFFA09A8E),
    surfaceContainerLowest = Color(0xFF0F0F0E),
    surfaceContainerLow = Color(0xFF181715),
    surfaceContainer = Color(0xFF1E1D1A),
    surfaceContainerHigh = Color(0xFF262421),
    surfaceContainerHighest = Color(0xFF2E2C28),
    outline = Color(0xFF6E685E),
    outlineVariant = Color(0xFF34312C),
    error = Color(0xFFE97B6D),
    onError = Color(0xFF3A0D06),
    errorContainer = Color(0xFF5C2018),
    onErrorContainer = Color(0xFFF7DDD7),
)

private val Sans = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.2.sp),
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

object AppTheme {
    val finance: FinanceColors
        @Composable get() = LocalFinanceColors.current
}

@Composable
fun PersonalExpenseTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFinanceColors provides if (dark) DarkFinance else LightFinance) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
