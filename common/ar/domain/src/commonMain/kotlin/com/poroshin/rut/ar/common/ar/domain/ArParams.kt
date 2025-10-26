package com.poroshin.rut.ar.common.ar.domain

data class ArObjectParams(
    val filePath: String,
    val widthMm: Float,
    val heightMm: Float,
    val depthMm: Float,
)

data class ArCoveringParams(
    val isFloor: Boolean,
    val patternUrl: String,
)
