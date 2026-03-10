package com.mdtuhinhasnat.smartwheelchairdatacollector.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MetaBlue,
    background = Color(0xFF18191A),
    surface = Color(0xFF242526),
    onPrimary = Color.White,
    onBackground = Color(0xFFE4E6EB),
    onSurface = Color(0xFFE4E6EB),
    error = DestroyRed
)

private val LightColorScheme = lightColorScheme(
    primary = MetaBlue,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    error = DestroyRed
)

@Composable
fun SmartWheelchairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
