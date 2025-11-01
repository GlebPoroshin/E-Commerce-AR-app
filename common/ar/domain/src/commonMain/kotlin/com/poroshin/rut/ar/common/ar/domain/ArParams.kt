package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.pdp.domain.ArPlacement

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
