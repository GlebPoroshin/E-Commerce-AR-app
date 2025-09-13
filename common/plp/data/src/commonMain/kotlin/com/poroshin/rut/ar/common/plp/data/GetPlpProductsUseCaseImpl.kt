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
                "https://www.freepnglogos.com/uploads/furniture-png/furniture-png-transparent-images-png-only-18.png",
                oldPrice = "24 990",
                discount = 28,
                rate = 4.6
            ),
            Product(
                1001L,
                "Кресло Loft",
                "Металл и кожа, минимализм",
                "12 400",
                "https://www.freepnglogos.com/uploads/furniture-png…air-png-clip-art-gallery-yopriceville-high-29.png",
                oldPrice = null,
                discount = null,
                rate = 4.2
            ),
            Product(
                1002L,
                "Стол Eames",
                "Стекло, бук, стиль mid-century",
                "22 990",
                "https://avatars.mds.yandex.net/i?id=c679a891842cc0f26b04b83eef425350c363da58-4382295-images-thumbs&n=13",
                oldPrice = "26 990",
                discount = 15,
                rate = 4.8
            ),
            Product(
                1003L,
                "Тумба Nova",
                "Компактное хранение",
                "7 990",
                "https://picsum.photos/seed/cabinet1/800/800",
                oldPrice = null,
                discount = null,
                rate = 4.0
            ),
            Product(
                1004L,
                "Лампа Orbit",
                "Тёплый свет для уюта",
                "3 490",
                "https://picsum.photos/seed/lamp1/800/800",
                oldPrice = "4 290",
                discount = 19,
                rate = 3.9
            ),
            Product(
                1005L,
                "Кровать Cloud",
                "Ортопедическое основание",
                "35 900",
                "https://picsum.photos/seed/bed1/800/800",
                oldPrice = "42 900",
                discount = 16,
                rate = 4.9
            ),
        )
    }
}
