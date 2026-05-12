package com.poroshin.rut.ar.common.ar.presentation.internal

import com.poroshin.rut.ar.common.ar.domain.ArTrackingStatus
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Platform-only scene state holder for Sceneform-specific data that has no equivalent
 * in the shared [com.poroshin.rut.ar.common.ar.presentation.ArViewModel].
 *
 * Responsibilities:
 *  - [trackingStatus] — ARCore camera tracking, driven by per-frame updates.
 *  - [isModelLoading] — Sceneform ModelRenderable async load progress.
 *  - [placedModels]   — count of TransformableNode instances in the Sceneform scene graph.
 *
 * Domain concerns (rotate, scale, place, clearAll, singleMode, errors) are owned by
 * [com.poroshin.rut.ar.common.ar.presentation.ArViewModel] and must NOT be added here.
 */
class ArSceneController : ViewModel() {

    data class UiState(
        val trackingStatus: ArTrackingStatus = ArTrackingStatus.SearchingSurface,
        val isModelLoading: Boolean = false,
        val placedModels: Int = 0,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    internal fun reportTracking(status: ArTrackingStatus) {
        _uiState.update { it.copy(trackingStatus = status) }
    }

    internal fun reportModelLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isModelLoading = isLoading) }
    }

    internal fun reportPlacedCount(count: Int) {
        _uiState.update { it.copy(placedModels = count) }
    }
}
