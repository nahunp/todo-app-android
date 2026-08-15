package com.nahunp.todoapp.data.repository

import com.nahunp.todoapp.core.datastore.TokenStore
import com.nahunp.todoapp.core.network.TodoApiService
import com.nahunp.todoapp.core.network.toApiException
import com.nahunp.todoapp.data.local.LocalTodoDataSource
import com.nahunp.todoapp.data.remote.dto.LoginRequestDto
import com.nahunp.todoapp.data.remote.dto.RegisterRequestDto
import com.nahunp.todoapp.domain.model.PasswordPolicy
import com.nahunp.todoapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: TodoApiService,
    private val tokenStore: TokenStore,
    private val localTodoDataSource: LocalTodoDataSource,
) : AuthRepository {

    override val isAuthenticated: Flow<Boolean> = tokenStore.tokenFlow.map { it != null }

    override suspend fun login(email: String, password: String) {
        try {
            val response = api.login(LoginRequestDto(email, password))
            tokenStore.setToken(response.accessToken)
        } catch (t: Throwable) {
            throw t.toApiException()
        }
    }

    override suspend fun register(email: String, password: String, captchaToken: String) {
        try {
            api.register(RegisterRequestDto(email, password, captchaToken))
        } catch (t: Throwable) {
            throw t.toApiException()
        }
    }

    override suspend fun getPasswordPolicy(): PasswordPolicy {
        val dto = try {
            api.getPasswordPolicy()
        } catch (t: Throwable) {
            throw t.toApiException()
        }
        return PasswordPolicy(
            requiredLength = dto.requiredLength,
            requireDigit = dto.requireDigit,
            requireLowercase = dto.requireLowercase,
            requireUppercase = dto.requireUppercase,
            requireNonAlphanumeric = dto.requireNonAlphanumeric,
            requiredUniqueChars = dto.requiredUniqueChars,
        )
    }

    override suspend fun logout() {
        tokenStore.clear()
        // Wipes the offline cache and any un-synced pending operations, so
        // a different account signing in on this device never sees a
        // stray previous account's lists (and nothing tries to push a
        // dead account's queued changes to whichever account logs in
        // next). See LocalTodoDataSource.clearAll's doc comment.
        localTodoDataSource.clearAll()
    }

    override suspend fun deleteAccount() {
        try {
            api.deleteAccount()
        } catch (t: Throwable) {
            throw t.toApiException()
        }
        // Doesn't clear the token itself — see the interface doc comment.
        // Callers (TodoListViewModel.deleteAccount()) call logout()
        // explicitly after this succeeds.
    }
}
