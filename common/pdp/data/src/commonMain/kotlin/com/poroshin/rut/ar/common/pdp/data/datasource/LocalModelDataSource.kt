package com.poroshin.rut.ar.common.pdp.data.datasource

import com.poroshin.rut.ar.common.cart.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class LocalModelDataSource(private val cartRepository: CartRepository) {

    suspend fun saveModelVersion(sku: Long, version: Int = 0) = withContext(Dispatchers.IO) {
        cartRepository.saveModelVersion(sku, version)
    }

    suspend fun checkModelVersion(sku: Long): Int? = withContext(Dispatchers.IO) {
        cartRepository.getModelVersion(sku)
    }

    suspend fun removeModelVersion(sku: Long) = withContext(Dispatchers.IO) {
        cartRepository.deleteModelVersion(sku)
    }

    suspend fun isModelFileExists(path: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            val metadata = SystemFileSystem.metadataOrNull(path)
            metadata?.isRegularFile == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteModelFile(path: Path) = withContext(Dispatchers.IO) {
        try {
            SystemFileSystem.delete(path, mustExist = false)
        } catch (_: Exception) {
            // ignore
        }
    }
}
