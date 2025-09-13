package com.poroshin.rut.ar.common.pdp.data

import com.poroshin.rut.ar.common.pdp.domain.ArInfo
import com.poroshin.rut.ar.common.pdp.domain.ArType
import com.poroshin.rut.ar.common.pdp.domain.GetPdpParams
import com.poroshin.rut.ar.common.pdp.domain.GetProductPageInfoUseCase
import com.poroshin.rut.ar.common.pdp.domain.OsType
import com.poroshin.rut.ar.common.pdp.domain.ProductPageInfo
import com.poroshin.rut.ar.common.pdp.domain.currentOs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class GetProductPageInfoUseCaseImpl : GetProductPageInfoUseCase {
    override suspend fun invoke(params: GetPdpParams): ProductPageInfo = withContext(Dispatchers.IO) {
        delay(800L)

        val imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0…582/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_01.jpg"

        val arUrl = when (currentOs()) {
            OsType.ANDROID -> "https://storage.yandexcloud.net/ar-app/models/609123.glb"
            OsType.IOS -> "https://storage.yandexcloud.net/ar-app/models/609123.usdz"
        }

        ProductPageInfo(
            sku = params.sku,
            name = "Диван Skandi",
            description = "Мягкий велюр, дубовые ножки",
            price = "18 000",
            images = listOf(imageUrl),
            oldPrice = "24 990",
            discount = 28,
            rating = 4.6,
            characteristics = mapOf(
                "Материал" to "Велюр, дуб",
                "Страна" to "Россия",
                "Гарантия" to "24 мес."
            ),
            stock = 12,
            deliveryInfo = "Доставим завтра",
            ar = ArInfo(
                arType = ArType.OBJECT,
                arRecourceUrl = arUrl
            )
        )
    }
}


