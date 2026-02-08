package com.poroshin.rut.ar.common.cart.presentation

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
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator
import com.poroshin.rut.ar.common.cart.presentation.model.CartAction
import com.poroshin.rut.ar.common.cart.presentation.model.CartEvent
import com.poroshin.rut.ar.common.cart.presentation.ui.CartScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CartFragment : Fragment() {

    interface Host {
        fun openMainTab()

        fun openMainProduct(sku: Long)
    }

    private val viewModel: CartViewModel by inject()
    private val router: Router by inject()
    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onEvent(CartEvent.OnCreate)
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
                    CartScreen(
                        state = state,
                        onIncrease = { snapshot -> viewModel.onEvent(CartEvent.OnIncrease(snapshot)) },
                        onDecrease = { sku -> viewModel.onEvent(CartEvent.OnDecrease(sku)) },
                        onRemove = { sku -> viewModel.onEvent(CartEvent.OnRemove(sku)) },
                        onItemClick = { sku -> viewModel.onEvent(CartEvent.OnItemClick(sku)) },
                        onMainClick = { viewModel.onEvent(CartEvent.OnMainClick) },
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
                    is CartAction.OpenPdp -> {
                        val host = activity as? Host
                        if (host != null) {
                            host.openMainProduct(action.sku)
                        } else {
                            navigator.navigateTo(
                                router = router,
                                key = NavigationTree.Pdp,
                                params = Bundle().apply { putLong("sku", action.sku) },
                            )
                        }
                    }

                    CartAction.SwitchToMainTab -> {
                        (activity as? Host)?.openMainTab()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): CartFragment = CartFragment()
    }
}
