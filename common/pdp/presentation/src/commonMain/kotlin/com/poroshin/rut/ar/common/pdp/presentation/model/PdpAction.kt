package com.poroshin.rut.ar.common.pdp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiAction
import kotlinx.io.files.Path

sealed class PdpAction : UiAction {
    data class OpenArObject(
        val filePath: Path,
        val width: Int,
        val height: Int,
        val depth: Int,
    ) : PdpAction()

    data class OpenArCovering(
        val isFloor: Boolean,
        val patternUrl: String,
    ) : PdpAction()
}