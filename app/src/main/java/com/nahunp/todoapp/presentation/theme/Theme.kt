package com.nahunp.todoapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// No dark-mode-specific palette in the web frontend yet either (styles.css
// has one fixed :root palette) — this darkColorScheme is a straight
// Material3 default, not a deliberate Cloud-Dancer-dark variant. Worth
// designing a real one before this app ships, not before then.
private val LightColors = lightColorScheme(
    primary = CloudDancerAccent,
    error = CloudDancerAccent2,
    background = CloudDancerBg,
    surface = CloudDancerSurface,
    onBackground = CloudDancerText,
    onSurface = CloudDancerText,
)

private val DarkColors = darkColorScheme(
    primary = CloudDancerAccent,
    error = CloudDancerAccent2,
)

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}

// Reused directly where the web frontend has a dedicated "success" color
// (completed items, .success banners) — Material3's ColorScheme has no
// built-in success slot, so this is a standalone constant, not part of
// the ColorScheme object above.
val SuccessColor: Color = CloudDancerSuccess
