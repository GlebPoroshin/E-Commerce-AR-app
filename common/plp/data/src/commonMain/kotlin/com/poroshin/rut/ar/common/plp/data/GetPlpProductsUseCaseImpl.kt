package com.poroshin.rut.ar.common.plp.data

import com.poroshin.rut.ar.common.core.BackendConfig
import com.poroshin.rut.ar.common.plp.domain.Product
import com.poroshin.rut.ar.common.plp.domain.GetPlpProductsUseCase
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class GetPlpProductsUseCaseImpl(
    private val httpClient: HttpClient,
) : GetPlpProductsUseCase {
    override suspend fun invoke(page: Int?): List<Product> = withContext(Dispatchers.IO) {
        runCatching {
            val response: List<PlpProductDto> = httpClient.get("${BackendConfig.apiBaseUrl()}/plp") {
                page?.let { parameter("page", it) }
            }.body()
            println("Backend Success: PLP fetched ${response.size} products from ${BackendConfig.apiBaseUrl()}")
            response.map { it.toDomain() }
        }.getOrElse {
            println("Backend Error: PLP fetch failed - ${it.message}")
            if (BackendConfig.isMockFallbackEnabled()) {
                mockProducts()
            } else {
                throw it
            }
        }
    }

    private fun mockProducts(): List<Product> {
        val image = "https://images.weserv.nl/?url=www.svgrepo.com/show/508699/landscape-placeholder.svg&output=png&w=600"
        return listOf(
            Product(
                1000L,
                "Диван «Осло»",
                "Трёхместный, обивка из плотного велюра, дубовые ножки",
                "42 990",
                image,
                oldPrice = "54 990",
                discount = 22,
                rate = 4.7
            ),
            Product(
                1001L,
                "Кресло «Хельсинки»",
                "Поворотное, экокожа, металлический каркас",
                "18 490",
                image,
                oldPrice = null,
                discount = null,
                rate = 4.3
            ),
            Product(
                1002L,
                "Обеденный стол «Берген»",
                "Раздвижной, массив бука, до 8 персон",
                "29 990",
                image,
                oldPrice = "36 500",
                discount = 18,
                rate = 4.8
            ),
            Product(
                1003L,
                "Стеллаж «Лофт»",
                "5 полок, металл и ЛДСП под дуб сонома",
                "9 990",
                image,
                oldPrice = null,
                discount = null,
                rate = 4.1
            ),
            Product(
                1004L,
                "Торшер «Норд»",
                "Регулируемая высота, тёплый свет 2700K",
                "5 490",
                image,
                oldPrice = "6 990",
                discount = 21,
                rate = 4.5
            ),
            Product(
                1005L,
                "Шкаф-купе «Альта»",
                "Двери с зеркалом, ширина 180 см, орех",
                "34 990",
                image,
                oldPrice = "41 990",
                discount = 17,
                rate = 4.4
            ),
            Product(
                1006L,
                "Журнальный столик «Мини»",
                "Круглая столешница, мрамор, латунные ножки",
                "12 290",
                image,
                oldPrice = null,
                discount = null,
                rate = 4.6
            ),
            Product(
                1007L,
                "Кровать «Сканди»",
                "160×200, мягкое изголовье, ортопедическое основание",
                "26 990",
                image,
                oldPrice = "32 490",
                discount = 17,
                rate = 4.7
            ),
        )
    }

    private fun PlpProductDto.toDomain(): Product {
        return Product(
            sku = sku,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl,
            oldPrice = oldPrice,
            discount = discount,
            rate = rate,
        )
    }
}

@Serializable
private data class PlpProductDto(
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rate: Double = 0.0,
)
