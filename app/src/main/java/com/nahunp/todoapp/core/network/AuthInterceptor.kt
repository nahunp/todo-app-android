package com.nahunp.todoapp.core.network

import com.nahunp.todoapp.core.datastore.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the stored bearer token to every request that has one. Deliberately
 * does NOT do the web frontend's session-expiry handling (clear token +
 * redirect to login on any 401) here — that bug (see the web repo's
 * fix/auth-loading-and-error-states PR) came from conflating "a 401 from
 * the login endpoint itself" with "an authenticated request's token was
 * rejected." Keep that distinction explicit in the repository layer
 * (AuthRepositoryImpl / TodoListRepositoryImpl), which knows which case
 * it's in, rather than guessing from the URL in a shared interceptor.
 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStore.getToken() }
        val request = chain.request().let { original ->
            if (token != null) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }
        }
        return chain.proceed(request)
    }
}
