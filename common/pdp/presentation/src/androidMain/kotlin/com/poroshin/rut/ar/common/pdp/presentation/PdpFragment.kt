package com.poroshin.rut.ar.common.pdp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpAction
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpEvent
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState
import org.koin.android.ext.android.inject

class PdpFragment : Fragment() {
    private val viewModel: PdpViewModel by inject<PdpViewModel>()

    private val skuArg: Long?
        get() = arguments?.getLong("sku")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sku = skuArg ?: 1000L
        viewModel.onEvent(PdpEvent.OnCreate(sku))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                PdpScreen(viewModel)
            }
        }
    }

    companion object {
        fun newInstance(): PdpFragment = PdpFragment()
    }
}

@Composable
private fun PdpScreen(viewModel: PdpViewModel) {
    val state by viewModel.viewState.collectAsState()
    val isDownloadedState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.viewAction.collect { action ->
            when (action) {
                is PdpAction.OpenArObject -> isDownloadedState.value = true
                else -> { /* no-op */ }
            }
        }
    }

    when (val viewState = state) {
        is PdpState.Loading -> CircularProgressIndicator()

        is PdpState.Content -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "Product Detail", style = MaterialTheme.typography.headlineLarge)
                Text(text = "SKU: ${viewState.product.sku}", style = MaterialTheme.typography.titleMedium)
                Text(text = viewState.product.name, style = MaterialTheme.typography.titleSmall)
                Text(text = "Price: ${viewState.product.price}", style = MaterialTheme.typography.bodyMedium)

                viewState.loadingState?.let { percent ->
                    LinearProgressIndicator(progress = percent / 100f)
                    Text(text = "Loading: $percent%", style = MaterialTheme.typography.bodySmall)
                }

                if (isDownloadedState.value) {
                    Text(text = "Модель скачана", style = MaterialTheme.typography.titleMedium)
                } else {
                    Button(
                        onClick = {
                            viewModel.onEvent(
                                PdpEvent.OnModelLoad(state = viewState)
                            )
                        }
                    ) {
                        Text("Скачать модель")
                    }
                }
            }
        }
    }
}
