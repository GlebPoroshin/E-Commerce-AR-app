package com.poroshin.rut.ar.common.pdp.data.usecase

import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveSkuQuantityUseCase
import com.poroshin.rut.ar.common.core.BackendConfig
import com.poroshin.rut.ar.common.pdp.data.currentOs
import com.poroshin.rut.ar.common.pdp.domain.ArInfo
import com.poroshin.rut.ar.common.pdp.domain.ArPlacement
import com.poroshin.rut.ar.common.pdp.domain.ArType
import com.poroshin.rut.ar.common.pdp.domain.GetPdpParams
import com.poroshin.rut.ar.common.pdp.domain.OsType
import com.poroshin.rut.ar.common.pdp.domain.usecase.GetProductPageInfoUseCase
import com.poroshin.rut.ar.common.pdp.domain.ProductPageInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

class GetProductPageInfoUseCaseImpl(
    private val httpClient: HttpClient,
    private val observeSkuQuantity: ObserveSkuQuantityUseCase,
) : GetProductPageInfoUseCase {
    override suspend fun invoke(params: GetPdpParams): ProductPageInfo = withContext(Dispatchers.IO) {
        val product = runCatching {
            val response: ProductPageInfoDto = httpClient.get("${BackendConfig.apiBaseUrl()}/pdp/${params.sku}") {
                parameter("osType", currentOs().name)
            }.body()

            println("Backend Success: PDP fetched product ${params.sku} from ${BackendConfig.apiBaseUrl()}")
            response.toDomain()
        }.getOrElse {
            println("Backend Error: PDP fetch failed for sku ${params.sku} - ${it.message}")
            if (BackendConfig.isMockFallbackEnabled()) {
                mockProduct(params)
            } else {
                throw it
            }
        }

        val cartQuantity = runCatching { observeSkuQuantity(params.sku).first() }.getOrDefault(0)
        product.copy(cartQuantity = cartQuantity)
    }

    private fun mockProduct(params: GetPdpParams): ProductPageInfo {
        val image = "https://images.weserv.nl/?url=www.svgrepo.com/show/508699/landscape-placeholder.svg&output=png&w=600"

        val arUrl = when (currentOs()) {
            OsType.ANDROID -> "https://storage.yandexcloud.net/ar-app/models/AR-Code-1683007596576.glb"
            OsType.IOS -> "https://storage.yandexcloud.net/ar-app/models/AR-Code-1683007596576.usdz"
        }

        val ar = ArInfo(
            arType = ArType.OBJECT,
            placement = ArPlacement.ANY_HORIZONTAL,
            arResourceUrl = arUrl,
            version = 1002,
            width = 950f,
            height = 1000f,
            depth = 950f,
        )

        val catalog = mapOf<Long, ProductPageInfo>(
            1000L to ProductPageInfo(
                sku = 1000L,
                name = "Диван «Осло»",
                description = "Трёхместный диван в скандинавском стиле. Каркас из массива берёзы, обивка из износостойкого велюра, мягкие подушки с эффектом памяти. Идеально для гостиной 16-25 м².",
                price = "42 990",
                images = listOf(image),
                oldPrice = "54 990",
                discount = 22,
                rating = 4.7,
                characteristics = mapOf(
                    "Материал обивки" to "Велюр",
                    "Каркас" to "Массив берёзы",
                    "Длина" to "215 см",
                    "Глубина" to "92 см",
                    "Цвет" to "Графит",
                    "Гарантия" to "24 мес.",
                ),
                stock = 7,
                deliveryInfo = "Доставим завтра до 18:00",
                ar = ar,
            ),
            1001L to ProductPageInfo(
                sku = 1001L,
                name = "Кресло «Хельсинки»",
                description = "Поворотное кресло на 360°, обивка из мягкой экокожи, металлический каркас цвета чёрный матовый. Регулируемая высота, нагрузка до 120 кг.",
                price = "18 490",
                images = listOf(image),
                oldPrice = null,
                discount = null,
                rating = 4.3,
                characteristics = mapOf(
                    "Материал обивки" to "Экокожа",
                    "Каркас" to "Сталь",
                    "Высота" to "82-92 см",
                    "Цвет" to "Бежевый",
                    "Макс. нагрузка" to "120 кг",
                ),
                stock = 14,
                deliveryInfo = "Доставим послезавтра",
                ar = ar,
            ),
            1002L to ProductPageInfo(
                sku = 1002L,
                name = "Обеденный стол «Берген»",
                description = "Раздвижной стол из массива бука, с возможностью трансформации от 140 до 200 см. Лаковое покрытие защищает поверхность от влаги и царапин.",
                price = "29 990",
                images = listOf(image),
                oldPrice = "36 500",
                discount = 18,
                rating = 4.8,
                characteristics = mapOf(
                    "Материал" to "Массив бука",
                    "Длина" to "140-200 см",
                    "Ширина" to "90 см",
                    "Высота" to "76 см",
                    "Покрытие" to "Лак матовый",
                ),
                stock = 4,
                deliveryInfo = "Доставим за 3-5 дней",
                ar = ar,
            ),
            1003L to ProductPageInfo(
                sku = 1003L,
                name = "Стеллаж «Лофт»",
                description = "Открытый стеллаж на 5 полок из ЛДСП с декором под дуб сонома и металлическим каркасом. Подойдёт для книг, декора или хранения техники.",
                price = "9 990",
                images = listOf(image),
                oldPrice = null,
                discount = null,
                rating = 4.1,
                characteristics = mapOf(
                    "Материал полок" to "ЛДСП 22 мм",
                    "Каркас" to "Металл, порошковая окраска",
                    "Высота" to "180 см",
                    "Ширина" to "80 см",
                    "Глубина" to "35 см",
                ),
                stock = 23,
                deliveryInfo = "Доставим завтра до 21:00",
                ar = ar,
            ),
            1004L to ProductPageInfo(
                sku = 1004L,
                name = "Торшер «Норд»",
                description = "Напольный торшер с регулируемой высотой 140-170 см. Тёплый свет 2700K, абажур из льняной ткани, плавная регулировка яркости.",
                price = "5 490",
                images = listOf(image),
                oldPrice = "6 990",
                discount = 21,
                rating = 4.5,
                characteristics = mapOf(
                    "Высота" to "140-170 см",
                    "Цвет. температура" to "2700K",
                    "Тип цоколя" to "E27",
                    "Макс. мощность" to "60 Вт",
                    "Материал абажура" to "Лён",
                ),
                stock = 18,
                deliveryInfo = "Доставим завтра",
                ar = ar,
            ),
            1005L to ProductPageInfo(
                sku = 1005L,
                name = "Шкаф-купе «Альта»",
                description = "Двухдверный шкаф-купе с зеркальной вставкой. Внутри — штанга, 5 полок и ящик для мелочей. Бесшумная роликовая система.",
                price = "34 990",
                images = listOf(image),
                oldPrice = "41 990",
                discount = 17,
                rating = 4.4,
                characteristics = mapOf(
                    "Ширина" to "180 см",
                    "Высота" to "220 см",
                    "Глубина" to "60 см",
                    "Цвет корпуса" to "Орех",
                    "Гарантия" to "36 мес.",
                ),
                stock = 2,
                deliveryInfo = "Доставим за 5-7 дней",
                ar = ar,
            ),
            1006L to ProductPageInfo(
                sku = 1006L,
                name = "Журнальный столик «Мини»",
                description = "Круглая столешница из натурального мрамора с золотистыми прожилками, латунные ножки. Лаконичный акцент для гостиной или спальни.",
                price = "12 290",
                images = listOf(image),
                oldPrice = null,
                discount = null,
                rating = 4.6,
                characteristics = mapOf(
                    "Материал столешницы" to "Мрамор",
                    "Ножки" to "Латунь",
                    "Диаметр" to "55 см",
                    "Высота" to "45 см",
                ),
                stock = 9,
                deliveryInfo = "Доставим послезавтра",
                ar = ar,
            ),
            1007L to ProductPageInfo(
                sku = 1007L,
                name = "Кровать «Сканди»",
                description = "Двуспальная кровать 160×200 с мягким изголовьем и ортопедическим основанием. Каркас из массива сосны, обивка из рогожки.",
                price = "26 990",
                images = listOf(image),
                oldPrice = "32 490",
                discount = 17,
                rating = 4.7,
                characteristics = mapOf(
                    "Размер спального места" to "160×200 см",
                    "Каркас" to "Массив сосны",
                    "Изголовье" to "Рогожка серая",
                    "Основание" to "Ортопедическое",
                    "Гарантия" to "24 мес.",
                ),
                stock = 6,
                deliveryInfo = "Доставим за 3-5 дней",
                ar = ar,
            ),
        )

        return catalog[params.sku] ?: ProductPageInfo(
            sku = params.sku,
            name = "Товар",
            description = "Описание товара недоступно",
            price = "—",
            images = listOf(image),
            oldPrice = null,
            discount = null,
            rating = 0.0,
            characteristics = emptyMap(),
            stock = null,
            deliveryInfo = null,
            ar = ar,
        )
    }

    private fun ProductPageInfoDto.toDomain(): ProductPageInfo {
        return ProductPageInfo(
            sku = sku,
            name = name,
            description = description,
            price = price,
            images = images,
            oldPrice = oldPrice,
            discount = discount,
            rating = rating,
            characteristics = characteristics,
            stock = stock,
            deliveryInfo = deliveryInfo,
            ar = ar?.toDomain(),
        )
    }

    private fun ArInfoDto.toDomain(): ArInfo {
        return ArInfo(
            version = version,
            arType = arType,
            placement = placement,
            arResourceUrl = arResourceUrl,
            width = width,
            height = height,
            depth = depth,
        )
    }
}

@Serializable
private data class ProductPageInfoDto(
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val images: List<String>,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rating: Double = 0.0,
    val characteristics: Map<String, String> = emptyMap(),
    val stock: Int? = null,
    val deliveryInfo: String? = null,
    val ar: ArInfoDto? = null,
)

@Serializable
private data class ArInfoDto(
    val version: Int? = null,
    val arType: ArType,
    val placement: ArPlacement,
    @JsonNames("arResourceUrl", "arRecourceUrl")
    val arResourceUrl: String,
    val width: Float,
    val height: Float,
    val depth: Float? = null,
)
