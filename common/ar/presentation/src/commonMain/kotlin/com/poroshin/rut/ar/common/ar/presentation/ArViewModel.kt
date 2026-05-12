package com.poroshin.rut.ar.common.ar.presentation

import com.poroshin.rut.ar.common.ar.domain.model.ArPose
import com.poroshin.rut.ar.common.ar.domain.result.RotationResult
import com.poroshin.rut.ar.common.ar.domain.result.ScaleResult
import com.poroshin.rut.ar.common.ar.domain.usecase.PlaceModelUseCase
import com.poroshin.rut.ar.common.ar.domain.usecase.RotateModelUseCase
import com.poroshin.rut.ar.common.ar.domain.usecase.ScaleModelUseCase
import com.poroshin.rut.ar.common.ar.presentation.model.ArAction
import com.poroshin.rut.ar.common.ar.presentation.model.ArEvent
import com.poroshin.rut.ar.common.ar.presentation.model.ArState
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ArViewModel(
    private val rotateModel: RotateModelUseCase,
    private val scaleModel: ScaleModelUseCase,
    private val placeModel: PlaceModelUseCase,
    initialModelUrl: String = "",
) : SharedViewModel<ArState, ArEvent, ArAction>(
    initialState = ArState(modelUrl = initialModelUrl)
) {

    private object Resolver : KoinComponent
    constructor() : this(
        rotateModel = Resolver.get(),
        scaleModel = Resolver.get(),
        placeModel = Resolver.get(),
    )

    override suspend fun handleEvent(event: ArEvent) {
        when (event) {
            is ArEvent.OnCreate -> onCreate()
            is ArEvent.RotateObject -> onRotate(event.deltaDegrees)
            is ArEvent.ScaleObject -> onScale(event.factor)
            is ArEvent.PlaceObject -> onPlace(event.pose)
            ArEvent.ClearAll -> onClearAll()
            ArEvent.RetryModelLoad -> onRetryLoad()
            is ArEvent.SetSingleMode -> onSetSingleMode(event.enabled)
            is ArEvent.ShowSceneError -> onShowSceneError(event.message)
        }
    }

    private fun onCreate() {
        val url = currentState.modelUrl
        if (url.isBlank()) {
            sendAction(ArAction.ShowError("No model URL provided."))
            return
        }
        updateState { copy(isLoading = true, error = null) }
        sendAction(
            ArAction.LogTelemetry(
                name = "ar_session_created",
                params = mapOf("model_url" to url),
            )
        )
    }

    private fun onRotate(deltaDegrees: Float) {
        val result = rotateModel(currentState.currentRotation, deltaDegrees)
        when (result) {
            is RotationResult.Updated -> updateState { copy(currentRotation = result.newRotation) }
            is RotationResult.Rejected -> sendAction(ArAction.ShowError(result.reason))
        }
    }

    private fun onScale(factor: Float) {
        val result = scaleModel(currentState.currentScale, factor)
        when (result) {
            is ScaleResult.Updated -> updateState { copy(currentScale = result.newScale) }
            is ScaleResult.Rejected -> sendAction(ArAction.ShowError(result.reason))
        }
    }

    private fun onPlace(pose: ArPose) {
        // TODO(Phase 3B): halfExtents and existing AABBs must be supplied by the platform
        // AR session (Sceneform node bounds). For now, using a zero-size AABB so collision
        // detection is effectively disabled until wired in Phase 3B.
        // The placement policy check (planeType) also requires platform data;
        // skipping it here and delegating full validation to ArSceneController in Phase 3B.
        val existing = currentState.placedObjects

        if (currentState.isSingleMode && existing.isNotEmpty()) {
            updateState { copy(placedObjects = listOf(pose)) }
            sendAction(
                ArAction.LogTelemetry(
                    name = "ar_object_placed",
                    params = mapOf("mode" to "single"),
                )
            )
            return
        }

        updateState { copy(placedObjects = placedObjects + pose) }
        sendAction(
            ArAction.LogTelemetry(
                name = "ar_object_placed",
                params = mapOf("mode" to "multi", "count" to (existing.size + 1).toString()),
            )
        )
    }

    private fun onClearAll() {
        updateState {
            copy(
                placedObjects = emptyList(),
                currentScale = 1.0f,
                currentRotation = floatArrayOf(0f, 0f, 0f),
            )
        }
        sendAction(ArAction.TriggerClearScene)
        sendAction(ArAction.LogTelemetry(name = "ar_clear_all"))
    }

    private fun onRetryLoad() {
        updateState { copy(isLoading = true, error = null, isModelLoaded = false) }
        sendAction(ArAction.TriggerReloadModel)
        sendAction(ArAction.LogTelemetry(name = "ar_model_retry"))
    }

    private fun onSetSingleMode(enabled: Boolean) {
        updateState { copy(isSingleMode = enabled) }
    }

    private fun onShowSceneError(message: String) {
        updateState { copy(error = message) }
        sendAction(ArAction.ShowError(message))
    }
}
