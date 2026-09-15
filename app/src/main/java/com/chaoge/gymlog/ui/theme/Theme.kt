package com.chaoge.gymlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF2563EB)
val PrimaryDark = Color(0xFF1D4ED8)
val PrimaryBg = Color(0xFFEFF6FF)
val PageBg = Color(0xFFFAFAFA)
val ContentBg = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF171717)
val TextSecondary = Color(0xFF737373)
val DividerColor = Color(0xFFE5E7EB)
val DoneColor = Color(0xFF16A34A)
val WarnColor = Color(0xFFF59E0B)
val DangerColor = Color(0xFFDC2626)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryBg,
    onPrimaryContainer = PrimaryDark,
    secondary = Primary,
    onSecondary = Color.White,
    background = PageBg,
    onBackground = TextPrimary,
    surface = ContentBg,
    onSurface = TextPrimary,
    surfaceVariant = DividerColor,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = DangerColor
)

@Composable
fun GymLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
