package soft.divan.financemanager.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/** Ключ состояния корневого графа в холдере [LocalDataHost]. */
private const val MAIN_GRAPH_STATE_KEY = "main-graph"

/**
 * Рисует выбранный [resolveLocalDataScreen] экран хоста.
 *
 * Замок и экран потери ключа **заменяют** граф: под ними он не строится. Но его saveable-состояние
 * (стеки навигации, введённое в `rememberSaveable`) хранится в холдере и возвращается после
 * разблокировки, а ViewModel экранов ждут в [mainGraph] — пользователь возвращается туда же, где
 * был.
 *
 * Если данные стёрли, пока граф скрыт (поколение [dataGeneration] сменилось), сессия графа
 * сбрасывается целиком — и состояние, и ViewModel: старый стек указывал бы на стёртые данные, а
 * старые ViewModel показали бы их до первой новой эмиссии. Пока сброс не выполнен, граф не
 * строится — вместо него пустой фон, как в [LocalDataScreen.NOTHING]. Стирание распознаётся по
 * поколению, а не по событию экрана замка: стереть данные может и фоновый выход из аккаунта, а
 * гейт открывает уже само снятие PIN, раньше события.
 *
 * @param dataGeneration текущее поколение локальных данных.
 * @param mainGraph сессия графа: [main] получает её хранилище как `LocalViewModelStoreOwner`.
 * @param lockScreen экран вместо графа: замок ([LocalDataScreen.UI_LOCK],
 *   [LocalDataScreen.DATA_LOCK]) или потеря ключа ([LocalDataScreen.KEY_LOST]).
 */
@Composable
internal fun LocalDataHost(
    screen: LocalDataScreen,
    dataGeneration: Long,
    mainGraph: MainGraphViewModel,
    lockScreen: @Composable (screen: LocalDataScreen) -> Unit,
    main: @Composable () -> Unit
) {
    val mainGraphState = rememberSaveableStateHolder()
    val stale = mainGraph.isStale(dataGeneration, staysShown = screen == LocalDataScreen.MAIN)

    // Ключ и по поколению: сброс принимает поколение, которое было при запуске эффекта, и новое
    // стирание до его выполнения должно запустить сброс ещё раз
    LaunchedEffect(stale, dataGeneration) {
        if (stale) {
            mainGraphState.removeState(MAIN_GRAPH_STATE_KEY)
            mainGraph.discard(dataGeneration)
        }
    }

    val shown = if (stale && screen == LocalDataScreen.MAIN) LocalDataScreen.NOTHING else screen
    when (shown) {
        LocalDataScreen.NOTHING -> Unit

        LocalDataScreen.MAIN -> mainGraphState.SaveableStateProvider(MAIN_GRAPH_STATE_KEY) {
            DisposableEffect(dataGeneration) {
                mainGraph.onShown(dataGeneration)
                onDispose { mainGraph.onHidden() }
            }
            CompositionLocalProvider(LocalViewModelStoreOwner provides mainGraph, content = main)
        }

        LocalDataScreen.KEY_LOST, LocalDataScreen.DATA_LOCK, LocalDataScreen.UI_LOCK -> lockScreen(shown)
    }
}
