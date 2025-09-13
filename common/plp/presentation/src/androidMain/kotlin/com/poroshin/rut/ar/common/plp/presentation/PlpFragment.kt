package com.poroshin.rut.ar.common.plp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.terrakok.cicerone.Router
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator
import com.poroshin.rut.ar.common.pdp.domain.PdpParams
import com.poroshin.rut.ar.common.plp.presentation.model.PlpAction
import com.poroshin.rut.ar.common.plp.presentation.model.PlpEvent
import com.poroshin.rut.ar.common.plp.presentation.ui.PlpScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PlpFragment : Fragment() {

    private val viewModel: PlpViewModel by inject<PlpViewModel>()
    private val router: Router by inject<Router>()
    private val navigator: Navigator by inject<Navigator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onEvent(PlpEvent.OnCreate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                PlpScreen(viewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewAction.collect { action ->
                when (action) {
                    is PlpAction.OpenPdp -> navigator.navigateTo(
                        router = router,
                        key = NavigationTree.Pdp,
                        params = PdpParams(action.sku).bundleAuto(),
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(): PlpFragment = PlpFragment()
    }
}
