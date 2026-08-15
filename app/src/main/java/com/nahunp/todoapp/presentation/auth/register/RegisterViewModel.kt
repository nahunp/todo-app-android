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
 * CAPTCHA is real now, not a placeholder — TurnstileCaptchaView loads the
 * web repo's frontend/public/mobile-captcha.html in a WebView and hands
 * the resulting token to onCaptchaTokenReceived below. See that file's
 * own comment and the web repo's CLAUDE.md, "Multi-client architecture,"
 * for the full reasoning (no backend change needed — the backend never
 * cared where a captchaToken came from).
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }

    // A blank token means the widget hasn't produced one yet, or a
    // previously-issued one expired/errored (see mobile-captcha.html's
    // expired-callback/error-callback, both of which send "" back) —
    // either way, submit() below refuses to proceed without a real one.
    fun onCaptchaTokenReceived(token: String) = _uiState.update { it.copy(captchaToken = token) }

    fun submit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password required") }
            return
        }
        if (state.captchaToken.isBlank()) {
            _uiState.update { it.copy(error = "Please complete the verification challenge.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.register(state.email, state.password, state.captchaToken)
                _uiState.update { it.copy(isLoading = false, registeredSuccessfully = true) }
            } catch (e: ApiException) {
                // A Turnstile token is single-use — a failed submit (e.g.
                // a weak password rejected by Identity) needs a fresh
                // one, not a stale token that'll just fail verification
                // again on retry. Same reasoning as the web frontend's
                // register.ts, which resets its widget on error for
                // exactly this reason; there's no equivalent "reset" call
                // for this WebView-hosted widget, so the whole captcha
                // state just gets cleared, forcing the WebView's own
                // reload (see RegisterScreen) to get a new token.
                _uiState.update {
                    it.copy(isLoading = false, error = e.message, captchaToken = "", captchaResetKey = it.captchaResetKey + 1)
                }
            }
        }
    }
}
