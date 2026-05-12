package com.poroshin.rut.ar.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.poroshin.rut.ar.common.ar.presentation.ArViewModel
import com.poroshin.rut.ar.common.ar.presentation.model.ArEvent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

/**
 * Изолированный MVI-тест AR-контролов.
 *
 * Использует createComposeRule (без Activity) — обходит ограничение
 * "Activity already set content". Koin уже инициализирован в App.onCreate,
 * поэтому получаем ArViewModel через GlobalContext.
 *
 * Доказывает MVI-цикл:
 *   AssistChip click
 *   → ArEvent.SetSingleMode(!currentState.isSingleMode)
 *   → ArViewModel.handleEvent
 *   → updateState { copy(isSingleMode = enabled) }
 *   → viewState StateFlow reemits
 *   → Compose recomposition
 *   → AssistChip label меняется: "Режим: одна" ↔ "Режим: несколько"
 */
@RunWith(AndroidJUnit4::class)
class ArMviTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun arModeSwitchMviCycleTest() {
        // Гарантируем, что Application (и Koin) уже инициализированы
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        // Получаем ArViewModel из Koin GlobalContext
        val viewModel = GlobalContext.get().get<ArViewModel>()

        composeTestRule.setContent {
            val arState by viewModel.viewState.collectAsState()
            MaterialTheme {
                Column(
                    modifier = Modifier.testTag("ar_controls_root"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AssistChip(
                            modifier = Modifier.testTag("ar_mode_toggle_isolated"),
                            onClick = {
                                viewModel.onEvent(ArEvent.SetSingleMode(!arState.isSingleMode))
                            },
                            label = {
                                Text(if (arState.isSingleMode) "Режим: одна" else "Режим: несколько")
                            },
                        )
                    }
                }
            }
        }

        // Начальное состояние ArState: isSingleMode = true (default в ArState)
        composeTestRule.onNodeWithTag("ar_controls_root").assertIsDisplayed()
        composeTestRule.onNodeWithText("Режим: одна").assertIsDisplayed()

        // Первый тап → ArEvent.SetSingleMode(false) → updateState → "Режим: несколько"
        composeTestRule.onNodeWithTag("ar_mode_toggle_isolated").performClick()
        composeTestRule.onNodeWithText("Режим: несколько").assertIsDisplayed()

        // Второй тап → ArEvent.SetSingleMode(true) → "Режим: одна"
        composeTestRule.onNodeWithTag("ar_mode_toggle_isolated").performClick()
        composeTestRule.onNodeWithText("Режим: одна").assertIsDisplayed()
    }
}
