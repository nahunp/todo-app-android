package com.nahunp.todoapp.domain.model

/**
 * Mirrors GET /api/v1/auth/password-policy exactly — same reasoning as the
 * web frontend's PasswordPolicy interface (auth.service.ts): fetched, not
 * hardcoded, so a requirements checklist here can never drift from what
 * the backend actually enforces.
 */
data class PasswordPolicy(
    val requiredLength: Int,
    val requireDigit: Boolean,
    val requireLowercase: Boolean,
    val requireUppercase: Boolean,
    val requireNonAlphanumeric: Boolean,
    val requiredUniqueChars: Int,
)
