// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // AGP 9.x has Kotlin support built in — no separate
    // org.jetbrains.kotlin.android plugin needed (or allowed: applying it
    // fails the build with "no longer required since AGP 9.0"). Confirmed
    // live against real Gradle 9.7.0 + AGP 9.3.1, not assumed — see
    // CLAUDE.md's "Environment reality" section.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
