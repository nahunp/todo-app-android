package com.nahunp.todoapp.presentation.auth.register

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registeredSuccessfully: Boolean = false,
    val captchaToken: String = "",
    // Bumped whenever the captcha widget needs a fresh token (after a
    // failed submit — Turnstile tokens are single-use). RegisterScreen
    // keys the WebView composable on this, which forces Compose to
    // dispose and recreate it (and therefore reload the page and get a
    // new token) — there's no direct "reset" call available the way the
    // web frontend's turnstile.reset(widgetId) has for its own widget.
    val captchaResetKey: Int = 0,
)
