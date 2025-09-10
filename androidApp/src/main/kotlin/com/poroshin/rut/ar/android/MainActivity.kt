package com.poroshin.rut.ar.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.poroshin.rut.ar.android.ui.theme.MyApplicationTheme
import com.poroshin.rut.ar.common.plp.presentation.PlpAction
import com.poroshin.rut.ar.common.plp.presentation.PlpEvent
import com.poroshin.rut.ar.common.plp.presentation.PlpState
import com.poroshin.rut.ar.common.plp.presentation.PlpTestViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlpScreen()
                }
            }
        }
    }
}

@Composable
private fun PlpScreen(vm: PlpTestViewModel = koinViewModel()) {
    val state: PlpState by vm.viewState.collectAsState(initial = PlpState())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(androidx.compose.ui.unit.dp(12))
    ) {
        Text(text = "Counter: ${'$'}{state.counter}")
        Button(onClick = { vm.onEvent(PlpEvent.Increment) }) { Text("+") }
        Button(onClick = { vm.onEvent(PlpEvent.Decrement) }) { Text("-") }

        val action by vm.viewAction.collectAsState(initial = null)
        when (action) {
            is PlpAction.ShowLimitToast -> Text("Limit reached!")
            else -> {}
        }
    }
}

@Preview
@Composable
private fun PlpScreenPreview() {
    MyApplicationTheme {
        PlpScreen(vm = PlpTestViewModel())
    }
}
