package com.poroshin.rut.ar.common.pdp.data.network

import com.poroshin.rut.ar.common.core.DeviceIdProvider
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header

private const val HEADER = "X-Device-Id"

fun deviceIdPlugin(provider: DeviceIdProvider) = createClientPlugin("DeviceIdPlugin") {
    onRequest { request, _ ->
        if (request.headers[HEADER] == null) {
            request.header(HEADER, provider.deviceId())
        }
    }
}
