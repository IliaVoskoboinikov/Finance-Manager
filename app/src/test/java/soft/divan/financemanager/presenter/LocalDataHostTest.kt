package soft.divan.financemanager.presenter

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Хост поверх графа: замок и экран потери ключа скрывают граф, но не теряют его стек, ввод и
 * ViewModel; стирание данных, пока граф скрыт, сбрасывает всё это.
 *
 * Граф — настоящий `NavDisplay` с теми же декораторами, что у `RootNavDisplay`.
 */
@RunWith(RobolectricTestRunner::class)
// Своё Application: у приложения Hilt-App, которое подняло бы настоящий граф зависимостей
@Config(sdk = [35], application = Application::class)
class LocalDataHostTest {

    @get:Rule
    val compose = createComposeRule()

    private val mainGraph = MainGraphViewModel()
    private var screen by mutableStateOf(LocalDataScreen.MAIN)
    private var dataGeneration by mutableLongStateOf(0L)

    // Экземпляры ViewModel экрана в порядке появления; рекомпозиция не добавляет дублей
    private val probes = linkedSetOf<ProbeViewModel>()

    @Test
    fun `ui lock keeps the back stack and the typed input`() {
        showHostAndOpenDetailsWithInput()

        showScreen(LocalDataScreen.UI_LOCK)
        compose.onNodeWithText(LOCK).assertExists()
        compose.onNodeWithText(DETAILS_WITH_INPUT).assertDoesNotExist()
        showScreen(LocalDataScreen.MAIN)

        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
    }

    @Test
    fun `data lock keeps the back stack and the typed input`() {
        showHostAndOpenDetailsWithInput()

        showScreen(LocalDataScreen.DATA_LOCK)
        showScreen(LocalDataScreen.MAIN)

        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
    }

    @Test
    fun `screen view model survives the lock`() {
        showHostAndOpenDetailsWithInput()
        val before = probes.single()

        showScreen(LocalDataScreen.UI_LOCK)
        showScreen(LocalDataScreen.MAIN)

        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
        assertThat(probes).containsExactly(before)
        assertThat(before.cleared).isFalse()
    }

    @Test
    fun `data wiped under the lock starts the graph over and clears its view models`() {
        showHostAndOpenDetailsWithInput()
        val before = probes.single()

        showScreen(LocalDataScreen.UI_LOCK)
        // Фоновый выход по истёкшей сессии: данные стёрты, замок остаётся
        compose.runOnIdle { dataGeneration++ }
        showScreen(LocalDataScreen.MAIN)

        assertGraphStartedOver(before)
    }

    @Test
    fun `wipe that opens the gate in the same frame still starts the graph over`() {
        showHostAndOpenDetailsWithInput()
        val before = probes.single()

        showScreen(LocalDataScreen.UI_LOCK)
        // «Забыли PIN»: стирание и снятие PIN открывают гейт одним снимком — граф не должен
        // успеть построиться со старой сессией
        compose.runOnIdle {
            dataGeneration++
            screen = LocalDataScreen.MAIN
        }
        compose.waitForIdle()

        assertGraphStartedOver(before)
    }

    @Test
    fun `lost key without a wipe keeps the graph session`() {
        showHostAndOpenDetailsWithInput()

        showScreen(LocalDataScreen.KEY_LOST)
        compose.onNodeWithText(KEY_LOST).assertExists()
        // «Попробовать снова» прочитал ключ — данные целы
        showScreen(LocalDataScreen.MAIN)

        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
    }

    @Test
    fun `lost key followed by a wipe starts the graph over`() {
        showHostAndOpenDetailsWithInput()
        val before = probes.single()

        showScreen(LocalDataScreen.KEY_LOST)
        compose.runOnIdle { dataGeneration++ }
        showScreen(LocalDataScreen.MAIN)

        assertGraphStartedOver(before)
    }

    @Test
    fun `wipe in the frame the lock appears still starts the graph over`() {
        showHostAndOpenDetailsWithInput()
        val before = probes.single()

        // Граф ещё на экране, когда кадр меняет его на замок и уже несёт новое поколение
        compose.runOnIdle {
            screen = LocalDataScreen.UI_LOCK
            dataGeneration++
        }
        compose.waitForIdle()
        // Сразу, ещё под замком: ViewModel стёртых данных не ждут разблокировки
        assertThat(before.cleared).isTrue()
        showScreen(LocalDataScreen.MAIN)

        assertGraphStartedOver(before)
    }

    @Test
    fun `activity recreated under the lock keeps the graph session`() {
        val restoration = StateRestorationTester(compose)
        showHostAndOpenDetailsWithInput(restoration)

        showScreen(LocalDataScreen.UI_LOCK)
        restoration.emulateSavedInstanceStateRestore()
        showScreen(LocalDataScreen.MAIN)

        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
    }

    @Test
    fun `wipe after the activity was recreated under the lock starts the graph over`() {
        val restoration = StateRestorationTester(compose)
        showHostAndOpenDetailsWithInput(restoration)
        val before = probes.single()

        showScreen(LocalDataScreen.UI_LOCK)
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { dataGeneration++ }
        showScreen(LocalDataScreen.MAIN)

        assertGraphStartedOver(before)
    }

    @Test
    fun `wipe while the graph is shown is left to the graph itself`() {
        showHostAndOpenDetailsWithInput()
        val before = probes.single()

        // Выход из аккаунта при открытом графе: стек уводит на авторизацию сам граф
        compose.runOnIdle { dataGeneration++ }
        compose.waitForIdle()

        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
        assertThat(before.cleared).isFalse()

        // И скрыть его после этого — не повод сбрасывать: сессия уже на новом поколении
        showScreen(LocalDataScreen.UI_LOCK)
        showScreen(LocalDataScreen.MAIN)
        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
    }

    private fun assertGraphStartedOver(before: ProbeViewModel) {
        compose.onNodeWithText(HOME).assertExists()
        assertThat(before.cleared).isTrue()

        compose.onNodeWithText(HOME).performClick()
        compose.onNodeWithText(DETAILS_EMPTY).assertExists()
        assertThat(probes.last()).isNotSameAs(before)
    }

    /**
     * [restoration] — показать хост через него, чтобы потом сымитировать пересоздание Activity
     * (поворот, смена языка): `mainGraph` при этом тот же, как его держит хранилище Activity.
     */
    private fun showHostAndOpenDetailsWithInput(restoration: StateRestorationTester? = null) {
        val setContent: (@Composable () -> Unit) -> Unit =
            restoration?.let { it::setContent } ?: compose::setContent
        setContent {
            LocalDataHost(
                screen = screen,
                dataGeneration = dataGeneration,
                mainGraph = mainGraph,
                lockScreen = { shown ->
                    Text(
                        if (shown == LocalDataScreen.KEY_LOST) KEY_LOST else LOCK
                    )
                },
                main = { TestGraph() }
            )
        }
        compose.onNodeWithText(HOME).performClick()
        compose.onNodeWithText(DETAILS_EMPTY).performClick()
        compose.onNodeWithText(DETAILS_WITH_INPUT).assertExists()
    }

    private fun showScreen(next: LocalDataScreen) {
        compose.runOnIdle { screen = next }
        compose.waitForIdle()
    }

    /** Главный экран → экран с вводом (`rememberSaveable`) и ViewModel. */
    @Composable
    private fun TestGraph() {
        val backStack = rememberNavBackStack(HomeKey)
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeAt(backStack.lastIndex) },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<HomeKey> {
                    Text(HOME, Modifier.clickable { backStack.add(DetailsKey) })
                }
                entry<DetailsKey> { DetailsScreen() }
            }
        )
    }

    @Composable
    private fun DetailsScreen(probe: ProbeViewModel = viewModel()) {
        probes += probe
        var input by rememberSaveable { mutableStateOf("") }
        Text("$DETAILS_PREFIX$input", Modifier.clickable { input = TYPED })
    }

    // Не private: сериализатор object-ключа берётся рефлексией
    @Serializable
    data object HomeKey : NavKey

    @Serializable
    data object DetailsKey : NavKey

    /** ViewModel экрана: видно, тот ли это экземпляр и очищен ли он. */
    class ProbeViewModel : ViewModel() {
        var cleared = false
            private set

        override fun onCleared() {
            cleared = true
        }
    }

    private companion object {
        const val HOME = "home"
        const val LOCK = "lock"
        const val KEY_LOST = "key lost"
        const val TYPED = "typed"
        const val DETAILS_PREFIX = "details:"
        const val DETAILS_EMPTY = DETAILS_PREFIX
        const val DETAILS_WITH_INPUT = DETAILS_PREFIX + TYPED
    }
}
