package com.nahunp.todoapp.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nahunp.todoapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Web-frontend equivalent: authGuard (services/auth.guard.ts) — except
 * that's a *route* guard checked per-navigation, while this is a one-shot
 * "what should the FIRST screen be" check, because Compose Navigation's
 * NavHost needs a concrete startDestination at first composition, not a
 * reactive one. A real per-navigation guard (bouncing back to Login if the
 * token expires mid-session, matching the interceptor's 401 handling) is
 * still open — see CLAUDE.md.
 *
 * null = still checking (show a loading state, don't render NavHost yet);
 * non-null = resolved, safe to compose NavHost with this startDestination.
 */
@HiltViewModel
class AppEntryViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val isAuthenticated = authRepository.isAuthenticated.first()
            _startDestination.value = if (isAuthenticated) {
                Destination.TodoLists.route
            } else {
                Destination.Login.route
            }
        }
    }
}
