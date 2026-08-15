package com.nahunp.todoapp.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nahunp.todoapp.core.network.ApiException
import com.nahunp.todoapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }

    fun submit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password required") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.login(state.email, state.password)
                _uiState.update { it.copy(isLoading = false, loginSucceeded = true) }
            } catch (e: ApiException) {
                // Same reasoning as the web frontend's shared/http-error.ts
                // and the auth interceptor fix that shipped alongside it
                // (see the web repo's fix/auth-loading-and-error-states PR):
                // show the backend's actual reason, and isLoading has to
                // flip back to false on failure or the button stays stuck
                // disabled with no way to retry.
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
