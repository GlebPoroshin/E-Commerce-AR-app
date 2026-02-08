package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.pdp.domain.ArPlacement

data class ArObjectParams(
    val filePath: String,
    val widthMm: Float,
    val heightMm: Float,
    val depthMm: Float,
    val placement: ArPlacement,
    val placementPolicy: ArPlacement = placement,
    val distancePolicy: ArDistancePolicy = ArDistancePolicy.Default,
    val scalePolicy: ArScalePolicy = ArScalePolicy.Default,
    val cartItem: CartItemSnapshot? = null,
)

data class ArCoveringParams(
    val isFloor: Boolean,
    val patternUrl: String,
)
