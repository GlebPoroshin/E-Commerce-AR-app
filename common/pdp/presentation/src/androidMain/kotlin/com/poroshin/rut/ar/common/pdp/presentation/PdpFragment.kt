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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.terrakok.cicerone.Router
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpAction
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpEvent
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState
import org.koin.android.ext.android.inject
import kotlinx.coroutines.launch

class PdpFragment : Fragment() {
    private val viewModel: PdpViewModel by inject<PdpViewModel>()
    private val router: Router by inject<Router>()
    private val navigator: Navigator by inject<Navigator>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewAction.collect { action ->
                when (action) {
                    is PdpAction.OpenArViewer -> {
                        navigator.navigateTo(router = router, key = NavigationTree.Ar)
                    }
                }
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

    when (val viewState = state) {
        is PdpState.Loading -> CircularProgressIndicator()

        is PdpState.Content -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(viewState.product.sku.toString())

                Button(
                    onClick = {
                        viewModel.onEvent(
                            PdpEvent.OnModelLoad(
                                sku = viewState.product.sku,
                                url = viewState.product.ar?.arRecourceUrl ?: "",
                                version = viewState.product.ar?.version ?: 0
                            )
                        )
                    }
                ) {
                    if (viewState.loadingState == null) {
                        Text("Try to load model")
                    } else {
                        Text("Loading state: ${viewState.loadingState}")
                    }
                }
            }
        }
    }
}
