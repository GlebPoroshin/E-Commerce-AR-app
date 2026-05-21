package com.poroshin.rut.ar.common.pdp.data.network

import com.poroshin.rut.ar.common.core.DeviceIdProvider
import com.poroshin.rut.ar.common.core.NetworkError
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installSharedClient(
    deviceIdProvider: DeviceIdProvider,
) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
        )
    }

    install(deviceIdPlugin(deviceIdProvider))

    HttpResponseValidator {
        validateResponse { response: HttpResponse ->
            val status = response.status
            if (status.value < 400) return@validateResponse
            when (status) {
                HttpStatusCode.TooManyRequests -> {
                    val retryAfterSec = response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()
                    throw NetworkError.RateLimited(retryAfterSec)
                }
                else -> when (status.value) {
                    in 500..599 -> throw NetworkError.Server(status.value)
                    in 400..499 -> throw NetworkError.Client(status.value)
                }
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            when (cause) {
                is NetworkError -> throw cause
                is HttpRequestTimeoutException -> throw NetworkError.NoConnection(cause)
                is IOException -> throw NetworkError.NoConnection(cause)
                is ResponseException -> throw NetworkError.Unknown(cause)
                else -> Unit
            }
        }
    }
}
