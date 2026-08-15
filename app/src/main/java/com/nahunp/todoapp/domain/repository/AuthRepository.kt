package com.nahunp.todoapp.domain.repository

import com.nahunp.todoapp.domain.model.PasswordPolicy
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isAuthenticated: Flow<Boolean>

    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String, captchaToken: String)
    suspend fun getPasswordPolicy(): PasswordPolicy
    suspend fun logout()

    // Google Play policy requires apps that support account creation to
    // also offer account deletion (in-app or via a web page) — this is
    // the Android side of that (see the backend's DELETE /auth/account
    // and the web frontend's equivalent). Doesn't clear the token itself
    // — the caller does that via logout() after this succeeds, same
    // ordering reasoning as the web frontend's AuthService.deleteAccount().
    suspend fun deleteAccount()
}
