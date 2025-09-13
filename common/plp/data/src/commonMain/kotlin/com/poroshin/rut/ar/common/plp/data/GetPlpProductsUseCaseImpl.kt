package com.poroshin.rut.ar.common.plp.data

import com.poroshin.rut.ar.common.plp.domain.Product
import com.poroshin.rut.ar.common.plp.domain.GetPlpProductsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class GetPlpProductsUseCaseImpl : GetPlpProductsUseCase {
    override suspend fun invoke(page: Int?): List<Product> = withContext(Dispatchers.IO) {
        delay(1500L)

        listOf(
            Product(
                1000L,
                "Диван Skandi",
                "Мягкий велюр, дубовые ножки",
                "18 000",
                "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0…582/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_01.jpg",
                oldPrice = "24 990",
                discount = 28,
                rate = 4.6
            ),
            Product(
                1001L,
                "Кресло Loft",
                "Металл и кожа, минимализм",
                "12 400",
                "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/f_auto/q_auto/w_180/h_180/c_pad/b_white/d_photoiscoming.png/v1756302231/lmcode/ZNvykuHReEiS2JiysaTtfw/89428038.png",
                oldPrice = null,
                discount = null,
                rate = 4.2
            ),
            Product(
                1002L,
                "Стол Eames",
                "Стекло, бук, стиль mid-century",
                "22 990",
                "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0…401/lmcode/aoz4PQriekCcblHRW7hgKg/92106858_01.jpg",
                oldPrice = "26 990",
                discount = 15,
                rate = 4.8
            ),
            Product(
                1003L,
                "Тумба Nova",
                "Компактное хранение",
                "7 990",
                "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0…753/lmcode/0xsaFgwZ0U6BXLKbAk16uA/90782652_01.jpg",
                oldPrice = null,
                discount = null,
                rate = 4.0
            ),
            Product(
                1004L,
                "Лампа Orbit",
                "Тёплый свет для уюта",
                "3 490",
                "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/f_auto/q_auto/w_180/h_180/c_pad/b_white/d_photoiscoming.png/v1756301982/lmcode/hHEINX8z0EuCLF3_MgoIPg/89428030.png",
                oldPrice = "4 290",
                discount = 19,
                rate = 3.9
            ),
        )
    }
}
