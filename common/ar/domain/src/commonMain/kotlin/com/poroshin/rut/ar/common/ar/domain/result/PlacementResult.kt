package com.poroshin.rut.ar.common.ar.domain.result

import com.poroshin.rut.ar.common.ar.domain.model.ArPose

sealed class PlacementResult {
    data class Updated(val pose: ArPose) : PlacementResult()
    data class Rejected(val reason: String) : PlacementResult()
}
