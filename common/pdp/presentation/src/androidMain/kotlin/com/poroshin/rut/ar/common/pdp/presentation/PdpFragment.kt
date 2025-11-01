package com.poroshin.rut.ar.common.pdp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.terrakok.cicerone.Router
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams
import com.poroshin.rut.ar.common.ar.presentation.toBundle
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpAction
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpEvent
import com.poroshin.rut.ar.common.pdp.presentation.ui.PdpScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PdpFragment : Fragment() {
    private val viewModel: PdpViewModel by inject()
    private val router: Router by inject()
    private val navigator: Navigator by inject()

    private val skuArg: Long?
        get() = arguments?.getLong("sku")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sku = skuArg ?: 1000L
        viewModel.onEvent(PdpEvent.OnCreate(sku))
    }

    override fun onResume() {
        super.onResume()
        viewModel.onEvent(PdpEvent.OnResume)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val state by viewModel.viewState.collectAsState()
                    PdpScreen(
                        state = state,
                        onModelLoadClick = { contentState ->
                            viewModel.onEvent(PdpEvent.OnModelLoad(contentState))
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewAction.collect { action ->
                when (action) {
                    is PdpAction.OpenArObject -> {
                        val params = ArObjectParams(
                            filePath = action.filePath.toString(),
                            widthMm = action.width,
                            heightMm = action.height,
                            depthMm = action.depth,
                            placement = action.placement,
                        )
                        navigator.navigateTo(
                            router = router,
                            key = NavigationTree.Ar,
                            params = params.toBundle(),
                        )
                    }
                    is PdpAction.OpenArCovering -> {
                        // TODO: handle covering navigation when implemented.
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): PdpFragment = PdpFragment()
    }
}
