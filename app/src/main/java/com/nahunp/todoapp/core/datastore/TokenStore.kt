package com.nahunp.todoapp.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore, not SharedPreferences directly — DataStore is the current
 * Android-recommended replacement (async, type-safe, no synchronous disk
 * I/O on the main thread the way SharedPreferences.apply() can incur).
 * Same role as the web frontend's AuthService.getToken()/setToken()
 * (localStorage there) — one place that owns the access token, everything
 * else (the auth interceptor, ViewModels) reads through this.
 *
 * No refresh token support, same as the backend (see the web repo's
 * CLAUDE.md, "Open / not yet designed" — 60-minute access token only,
 * deliberate v1 scope, not an oversight here either).
 */
@Singleton
class TokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val tokenKey = stringPreferencesKey("access_token")

    val tokenFlow: Flow<String?> = dataStore.data.map { it[tokenKey] }

    suspend fun getToken(): String? = tokenFlow.first()

    suspend fun setToken(token: String) {
        dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(tokenKey) }
    }
}
