package com.poroshin.rut.ar.common.ar.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams
import com.poroshin.rut.ar.common.ar.presentation.internal.ArSceneController
import com.poroshin.rut.ar.common.ar.presentation.internal.ArSceneController.SceneError
import com.poroshin.rut.ar.common.ar.presentation.internal.ArSceneController.TrackingStatus
import com.poroshin.rut.ar.common.ar.presentation.internal.CustomArFragment
import com.poroshin.rut.ar.common.ar.presentation.toArObjectParams

class ARFragment : Fragment() {

    private val params: ArObjectParams? by lazy { arguments?.toArObjectParams() }
    private val sceneController: ArSceneController by viewModels()

    private val sceneTag = "ar.scene"
    private var sceneContainerId: Int = View.generateViewId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sceneContainerId = View.generateViewId()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }

        val sceneContainer = FragmentContainerView(requireContext()).apply {
            id = sceneContainerId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
        root.addView(sceneContainer)

        val existing = childFragmentManager.findFragmentByTag(sceneTag) as? CustomArFragment
        val targetFragment = existing ?: params?.let { CustomArFragment.newInstance(it) }
        if (targetFragment != null) {
            childFragmentManager.commitNow {
                setReorderingAllowed(true)
                replace(sceneContainerId, targetFragment, sceneTag)
            }
        }

        val overlay = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                    ) {
                        ArOverlay(
                            hasParams = params != null,
                            controller = sceneController,
                        )
                    }
                }
            }
        }
        root.addView(overlay)

        return root
    }

    companion object {
        fun newInstance(): ARFragment = ARFragment()
    }
}

@Composable
private fun ArOverlay(
    hasParams: Boolean,
    controller: ArSceneController,
) {
    val uiState by controller.uiState.collectAsState()
    val singleMode by controller.singleMode.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!hasParams) {
            MissingParamsMessage(
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            TopControls(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                isSingleMode = singleMode,
                uiState = uiState,
                onToggleMode = { controller.setSingleMode(!singleMode) },
                onClear = controller::requestClearAll,
            )
        }

        GestureHint(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )

        uiState.lastError?.let { error ->
            ErrorBanner(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                error = error,
                onRetry = controller::retryModelLoad,
            )
        }
    }
}

@Composable
private fun TopControls(
    modifier: Modifier,
    isSingleMode: Boolean,
    uiState: ArSceneController.UiState,
    onToggleMode: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = onToggleMode,
                label = { Text(if (isSingleMode) "Режим: одна" else "Режим: несколько") },
            )
            AssistChip(
                onClick = onClear,
                enabled = uiState.placedModels > 0,
                label = { Text("Очистить") },
            )
        }

        if (uiState.isModelLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(160.dp),
            )
        }

        val trackingMessage = when (uiState.trackingStatus) {
            TrackingStatus.Searching -> "Ищем подходящую плоскость — перемещайте устройство."
            TrackingStatus.Lost -> "Трекинг потерян. Наведите камеру на освещенную поверхность."
            TrackingStatus.Tracking -> null
        }
        trackingMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (uiState.placedModels > 0) {
            Text(
                text = "Объектов в сцене: ${uiState.placedModels}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun GestureHint(modifier: Modifier) {
    AssistChip(
        modifier = modifier,
        onClick = {},
        label = { Text("Тап — поставить • Long press — переместить • 2 пальца — вращать") },
    )
}

@Composable
private fun ErrorBanner(
    modifier: Modifier,
    error: SceneError,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = error.asMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (error == SceneError.ModelLoadingFailed) {
                Button(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
    }
}

@Composable
private fun MissingParamsMessage(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = "Не удалось открыть AR-сцену: отсутствуют параметры модели.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

private fun SceneError.asMessage(): String = when (this) {
    SceneError.ModelLoadingFailed -> "Не удалось загрузить модель. Проверьте файл и попробуйте снова."
    SceneError.Collision -> "Модели не должны пересекаться. Выберите другое место."
    SceneError.PlaneNotAllowed -> "Эта плоскость не подходит для размещения выбранного объекта."
    SceneError.ArNotAvailable -> "ARCore недоступен на устройстве. Попробуйте обновить сервисы Google Play."
}
