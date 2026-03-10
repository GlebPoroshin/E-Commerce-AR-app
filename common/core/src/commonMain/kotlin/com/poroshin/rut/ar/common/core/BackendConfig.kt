package com.poroshin.rut.ar.common.core

object BackendConfig {
    private var customBaseUrl: String? = null
    private var useMockFallback: Boolean = false

    fun setCustomBaseUrl(value: String?) {
        customBaseUrl = value
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf { it.isNotBlank() }
    }

    fun baseUrl(): String = customBaseUrl ?: defaultBaseUrl()

    fun apiBaseUrl(): String = "${baseUrl().trimEnd('/')}/api"

    fun setUseMockFallback(value: Boolean) {
        useMockFallback = value
    }

    fun isMockFallbackEnabled(): Boolean = useMockFallback
}

expect fun defaultBaseUrl(): String
