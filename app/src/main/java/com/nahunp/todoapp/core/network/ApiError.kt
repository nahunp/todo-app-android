package com.nahunp.todoapp.core.network

import com.nahunp.todoapp.data.remote.dto.ProblemDetailsDto
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException

/**
 * Kotlin/Retrofit equivalent of the web frontend's shared/http-error.ts —
 * same reasoning, same bug it was written to avoid: don't show the user a
 * bare HTTP status when the backend already sent a real reason in the
 * response body (ProblemDetails — see GlobalExceptionHandler.cs). Every
 * repository catching an HttpException should route through this rather
 * than surfacing exception.message() directly.
 */
class ApiException(val statusCode: Int, message: String) : Exception(message)

private val json = Json { ignoreUnknownKeys = true }

fun Throwable.toApiException(): ApiException = when (this) {
    is HttpException -> {
        val statusCode = code()
        val body: ResponseBody? = response()?.errorBody()
        val problem = body?.string()?.let { raw ->
            runCatching { json.decodeFromString<ProblemDetailsDto>(raw) }.getOrNull()
        }
        val message = problem?.detail
            ?: problem?.errors?.values?.flatten()?.joinToString(" ")?.takeIf { it.isNotBlank() }
            ?: problem?.title
            ?: "Something went wrong ($statusCode). Please try again."
        ApiException(statusCode, message)
    }
    else -> ApiException(0, "Couldn't reach the server. Check your connection and try again.")
}
