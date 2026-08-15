package com.nahunp.todoapp.presentation.theme

import androidx.compose.ui.graphics.Color

// Ported 1:1 from the web frontend's styles.css :root custom properties —
// keep these two files in sync by hand for now (see CLAUDE.md's Design
// system section for why there's no shared source of truth between a CSS
// file and a Kotlin file yet).
val CloudDancerBg = Color(0xFFF1EFE8)
val CloudDancerSurface = Color(0xFFF8F6F1)
val CloudDancerText = Color(0xFF201E1D)
val CloudDancerAccent = Color(0xFF4E76AC) // primary actions, links, focus
val CloudDancerAccent2 = Color(0xFFE14459) // high priority, destructive
val CloudDancerSuccess = Color(0xFF748F2B) // completed
