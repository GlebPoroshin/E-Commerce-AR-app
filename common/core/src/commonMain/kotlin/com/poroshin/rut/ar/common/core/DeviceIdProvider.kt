package com.poroshin.rut.ar.common.core

import com.russhwolf.settings.Settings
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface DeviceIdProvider {
    fun deviceId(): String
}

@OptIn(ExperimentalUuidApi::class)
class SettingsDeviceIdProvider(
    private val settings: Settings,
) : DeviceIdProvider {

    override fun deviceId(): String {
        val existing = settings.getStringOrNull(KEY)
        if (existing != null) return existing
        val generated = Uuid.random().toString()
        settings.putString(KEY, generated)
        return generated
    }

    private companion object {
        const val KEY = "device_id"
    }
}
