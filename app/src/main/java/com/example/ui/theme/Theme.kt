package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val ZummarColorScheme = darkColorScheme(
    primary = ZummarCyan,
    onPrimary = RoyalNavyDeep,
    secondary = ZummarGold,
    onSecondary = RoyalNavyDeep,
    tertiary = ZummarEmerald,
    onTertiary = RoyalNavyDeep,
    background = RoyalNavyDeep,
    onBackground = TextIvory,
    surface = RoyalNavySurface,
    onSurface = TextIvory,
    surfaceVariant = RoyalNavyCard,
    onSurfaceVariant = TextSilver,
    error = ZummarAlertRed,
    onError = TextIvory,
    outline = RoyalNavyBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = ZummarColorScheme,
            typography = Typography,
            content = content
        )
    }
}
