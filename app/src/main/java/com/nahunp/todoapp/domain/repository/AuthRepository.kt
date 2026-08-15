package com.nahunp.todoapp.domain.repository

import com.nahunp.todoapp.domain.model.PasswordPolicy
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isAuthenticated: Flow<Boolean>

    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String, captchaToken: String)
    suspend fun getPasswordPolicy(): PasswordPolicy
    suspend fun logout()
}
