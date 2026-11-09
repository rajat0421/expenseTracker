package com.yourmoney.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DeepGreen = Color(0xFF1B5E3C)
val DeepGreenDark = Color(0xFF123F29)
val SoftGreen = Color(0xFFDDEEE2)
val BackgroundLight = Color(0xFFF7F8F5)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1C1E1B)
val TextSecondary = Color(0xFF6B7268)
val ExpenseRed = Color(0xFFC75450)
val IncomeGreen = Color(0xFF1B5E3C)
val AccentGold = Color(0xFFC9A24B)

private val YourMoneyColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = Color.White,
    primaryContainer = SoftGreen,
    onPrimaryContainer = DeepGreenDark,
    secondary = AccentGold,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SoftGreen,
    onSurfaceVariant = TextSecondary,
    error = ExpenseRed,
)

val YourMoneyTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

@Composable
fun YourMoneyTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = YourMoneyColorScheme,
        typography = YourMoneyTypography,
        content = content,
    )
}
