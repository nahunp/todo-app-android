package com.nahunp.todoapp.presentation.auth.register

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

/**
 * NOTE: registration is not actually usable yet. The backend's
 * RegisterCommand requires a verified Cloudflare Turnstile captchaToken
 * (see the web repo's CLAUDE.md, Auth section) — Turnstile is a web
 * widget, there's no Android SDK for it. Passing an empty/fake token here
 * will just get a 400 from the backend every time. See CLAUDE.md's "Open
 * questions" for the real options (a WebView-hosted Turnstile challenge,
 * swap to a mobile-appropriate provider like Play Integrity / SafetyNet's
 * successor for this platform only, or drop the captcha requirement for
 * mobile clients and rely on rate-limiting instead) — needs a decision
 * before this screen is more than a placeholder.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

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
                // See the class doc comment — this captchaToken is a
                // placeholder and will be rejected server-side until the
                // open CAPTCHA question above is resolved.
                authRepository.register(state.email, state.password, captchaToken = "")
                _uiState.update { it.copy(isLoading = false, registeredSuccessfully = true) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
