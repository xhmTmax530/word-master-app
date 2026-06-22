package com.wordmaster.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = Teal700,
        onPrimary = Paper,
        secondary = Teal500,
        onSecondary = Paper,
        tertiary = Amber,
        background = Paper,
        onBackground = Ink900,
        surface = Paper,
        onSurface = Ink900,
        error = Coral,
    )

private val DarkColors =
    darkColorScheme(
        primary = Teal500,
        onPrimary = Ink900,
        secondary = Teal100,
        onSecondary = Ink900,
        tertiary = Amber,
        background = Ink900,
        onBackground = Paper,
        surface = Ink900,
        onSurface = Paper,
        error = Coral,
    )

@Composable
fun WordMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = WordMasterTypography,
        content = content,
    )
}
