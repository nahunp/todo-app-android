package com.nahunp.todoapp.presentation.auth.register

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registeredSuccessfully: Boolean = false,
)
