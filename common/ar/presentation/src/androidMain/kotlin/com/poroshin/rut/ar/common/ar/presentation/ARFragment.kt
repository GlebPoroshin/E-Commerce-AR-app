package com.poroshin.rut.ar.common.ar.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams
import com.poroshin.rut.ar.common.ar.presentation.toArObjectParams

class ARFragment : Fragment() {

    private val params: ArObjectParams? by lazy {
        arguments?.toArObjectParams()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                ArScreen(params = params)
            }
        }
    }

    companion object {
        fun newInstance(): ARFragment = ARFragment()
    }
}

@Composable
private fun ArScreen(params: ArObjectParams?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "AR Viewer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )

        if (params == null) {
            Text(
                text = "Нет данных для отображения AR модели.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Файл: ${params.filePath}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Ширина: ${params.widthMm} мм",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Высота: ${params.heightMm} мм",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Глубина: ${params.depthMm} мм",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
