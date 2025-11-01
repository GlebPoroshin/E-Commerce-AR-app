package com.poroshin.rut.ar.common.ar.domain

enum class ArPlacement {
    FLOOR,
    CEILING,
    ANY_HORIZONTAL,
    ANY_VERTICAL,
    ANY_SURFACE
}

data class ArObjectParams(
    val filePath: String,
    val widthMm: Float,
    val heightMm: Float,
    val depthMm: Float,
    val placement: ArPlacement,
)

data class ArCoveringParams(
    val isFloor: Boolean,
    val patternUrl: String,
)
