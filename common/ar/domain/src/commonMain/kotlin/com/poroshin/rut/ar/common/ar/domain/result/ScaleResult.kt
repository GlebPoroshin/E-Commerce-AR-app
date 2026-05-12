package com.poroshin.rut.ar.common.ar.domain.result

sealed class ScaleResult {
    data class Updated(val newScale: Float) : ScaleResult()
    data class Rejected(val reason: String) : ScaleResult()
}
