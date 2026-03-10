package com.poroshin.rut.ar.common.pdp.data.usecase

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
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

class GetProductPageInfoUseCaseImpl(
    private val httpClient: HttpClient,
) : GetProductPageInfoUseCase {
    override suspend fun invoke(params: GetPdpParams): ProductPageInfo = withContext(Dispatchers.IO) {
        runCatching {
            val response: ProductPageInfoDto = httpClient.get("${BackendConfig.apiBaseUrl()}/pdp/${params.sku}") {
                parameter("osType", currentOs().name)
            }.body()

            response.toDomain()
        }.getOrElse {
            if (BackendConfig.isMockFallbackEnabled()) {
                mockProduct(params)
            } else {
                throw it
            }
        }
    }

    private fun mockProduct(params: GetPdpParams): ProductPageInfo {
        val imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0…582/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_01.jpg"

        val arUrl = when (currentOs()) {
            OsType.ANDROID -> "https://storage.yandexcloud.net/ar-app/models/AR-Code-1683007596576.glb"
            OsType.IOS -> "https://storage.yandexcloud.net/ar-app/models/AR-Code-1683007596576.usdz"
        }

        return ProductPageInfo(
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
                placement = ArPlacement.ANY_HORIZONTAL,
                arResourceUrl = arUrl,
                version = 1002,
                width = 950f,
                height = 1000f,
                depth = 950f,
            )
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
