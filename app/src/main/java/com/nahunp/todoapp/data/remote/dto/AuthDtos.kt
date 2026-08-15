package com.nahunp.todoapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class LoginResponseDto(val accessToken: String, val expiresAt: String)

// captchaToken is required server-side (RegisterCommand) — Cloudflare
// Turnstile on the web frontend. There is no Android SDK equivalent of the
// web Turnstile widget; see CLAUDE.md's "Open questions" for how
// registration is meant to work from this app before this DTO's captchaToken
// field can actually be filled with something real.
@Serializable
data class RegisterRequestDto(val email: String, val password: String, val captchaToken: String)

@Serializable
data class PasswordPolicyDto(
    val requiredLength: Int,
    val requireDigit: Boolean,
    val requireLowercase: Boolean,
    val requireUppercase: Boolean,
    val requireNonAlphanumeric: Boolean,
    val requiredUniqueChars: Int,
)

// Matches ProblemDetails / ValidationProblemDetails from
// GlobalExceptionHandler.cs in the backend — same shape the web
// frontend's shared/http-error.ts parses. Parse this out of any non-2xx
// response body rather than showing a raw HTTP status to the user.
@Serializable
data class ProblemDetailsDto(
    val title: String? = null,
    val detail: String? = null,
    val status: Int? = null,
    val errors: Map<String, List<String>>? = null,
)
