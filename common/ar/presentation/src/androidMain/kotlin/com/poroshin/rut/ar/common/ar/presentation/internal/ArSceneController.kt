package com.poroshin.rut.ar.common.ar.presentation.internal

import com.poroshin.rut.ar.common.ar.domain.ArTrackingStatus
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ArSceneController : ViewModel() {

    sealed class SceneError {
        data object ModelLoadingFailed : SceneError()
        data object InvalidDimensions : SceneError()
        data object Collision : SceneError()
        data object PlaneNotAllowed : SceneError()
        data object ArNotAvailable : SceneError()
    }

    data class UiState(
        val trackingStatus: ArTrackingStatus = ArTrackingStatus.SearchingSurface,
        val isModelLoading: Boolean = false,
        val lastError: SceneError? = null,
        val isSingleMode: Boolean = true,
        val placedModels: Int = 0,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _singleMode = MutableStateFlow(true)
    val singleMode = _singleMode.asStateFlow()

    private val _clearAllRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val clearAllRequests = _clearAllRequests.asSharedFlow()

    private val _reloadRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val reloadRequests = _reloadRequests.asSharedFlow()

    fun setSingleMode(single: Boolean) {
        if (_singleMode.value == single) return
        _singleMode.value = single
        _uiState.update { it.copy(isSingleMode = single) }
    }

    fun requestClearAll() {
        _clearAllRequests.tryEmit(Unit)
    }

    fun retryModelLoad() {
        _reloadRequests.tryEmit(Unit)
        _uiState.update { it.copy(lastError = null, isModelLoading = true) }
    }

    internal fun reportTracking(status: ArTrackingStatus) {
        _uiState.update { it.copy(trackingStatus = status) }
    }

    internal fun reportModelLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isModelLoading = isLoading) }
    }

    internal fun reportError(error: SceneError?) {
        _uiState.update { it.copy(lastError = error) }
    }

    internal fun reportPlacedCount(count: Int) {
        _uiState.update { it.copy(placedModels = count) }
    }
}
