package soft.divan.financemanager.core.featureapi

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * Контракт фичи для Navigation 3.
 *
 * Каждая фича описывает свои экраны как `NavEntry` и добавляет их в общий `entryProvider`,
 * который собирает модуль `app` из мультибиндинга `Set<FeatureApi>`. Сами экраны при этом
 * остаются приватными: публичный контракт фичи — её `NavKey` из `:api` модуля.
 *
 * Каждый ключ должен быть зарегистрирован ровно один раз, поэтому фича регистрирует только
 * свои экраны и никогда — экраны соседней фичи; переход к соседу выполняется через
 * [Navigator.goTo] с её ключом.
 */
interface FeatureApi {

    /**
     * Регистрирует экраны фичи в общем `entryProvider`.
     *
     * @param scope билдер `entryProvider`, в который добавляются `NavEntry` фичи.
     * @param navigator навигация по общему back stack: переход на ключ и возврат назад.
     * @param modifier модификатор, который фича применяет к корню своих экранов.
     */
    fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier = Modifier
    )
}
