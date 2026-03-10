package com.poroshin.rut.ar.common.pdp.data.usecase

import com.poroshin.rut.ar.common.pdp.data.getDownloadPath
import com.poroshin.rut.ar.common.pdp.data.objectModelType
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import com.poroshin.rut.ar.common.pdp.domain.usecase.DeleteProductModelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DeleteProductModelUseCaseImpl(
    private val modelRepository: ModelRepository,
) : DeleteProductModelUseCase {

    override suspend fun invoke(sku: Long) = withContext(Dispatchers.IO) {
        val objectModelType = objectModelType()
        val path = getDownloadPath("ar/models/${sku}${objectModelType}")
        modelRepository.deleteModel(sku, path)
    }
}
