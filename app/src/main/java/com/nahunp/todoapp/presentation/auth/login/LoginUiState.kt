package com.nahunp.todoapp.presentation.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSucceeded: Boolean = false,
)
