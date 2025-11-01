package com.poroshin.rut.ar.common.pdp.data.datasource

import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class LocalModelDataSource(private val settings: Settings) {

    suspend fun saveModelVersion(sku: Long, version: Int = 0) = withContext(Dispatchers.IO) {
        settings.putInt(
            key = DEFAULT_KEY + sku,
            value = version,
        )
    }

    suspend fun checkModelVersion(sku: Long): Int? = withContext(Dispatchers.IO) {
        settings.getIntOrNull(DEFAULT_KEY + sku)
    }

    suspend fun isModelFileExists(path: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            val metadata = SystemFileSystem.metadataOrNull(path)
            metadata?.isRegularFile == true
        } catch (e: Exception) {
            false
        }
    }

    private val DEFAULT_KEY = "model_"
}
