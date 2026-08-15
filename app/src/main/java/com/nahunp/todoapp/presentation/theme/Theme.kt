package com.nahunp.todoapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// No dark-mode-specific palette in the web frontend yet either (styles.css
// has one fixed :root palette) — Cloud Dancer's brand standards were never
// designed with a dark variant in mind. A first attempt here
// (darkColorScheme() only overriding primary/error, leaving
// background/surface/onBackground/onSurface at Material3's stock dark
// defaults) shipped broken — confirmed live on a real device: several
// texts and buttons were unreadable, dark-on-dark or light-on-light,
// because the scheme was internally inconsistent (Cloud Dancer's light
// accent colors against Material3's generic dark surfaces, not a
// deliberately designed pairing). Forced to light-only until there's a
// real Cloud-Dancer-dark palette to replace this with, not just "flip a
// boolean and hope the generic defaults look fine."
private val LightColors = lightColorScheme(
    primary = CloudDancerAccent,
    error = CloudDancerAccent2,
    background = CloudDancerBg,
    surface = CloudDancerSurface,
    onBackground = CloudDancerText,
    onSurface = CloudDancerText,
)

@Composable
fun TodoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content,
    )
}

// Reused directly where the web frontend has a dedicated "success" color
// (completed items, .success banners) — Material3's ColorScheme has no
// built-in success slot, so this is a standalone constant, not part of
// the ColorScheme object above.
val SuccessColor: Color = CloudDancerSuccess
