package soft.divan.financemanager.feature.auth.api

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.FeatureApi

/**
 * Фича авторизации.
 *
 * Работает в двух сценариях, у каждого свой ключ и своё поведение после успешного входа:
 * - вложенный (настройки → профиль → [ProfileAuthKey]) — обычный [FeatureApi.registerEntries],
 *   после успеха возврат назад;
 * - корневой ([AuthKey]) — [registerRootEntries], после успеха хост уводит на главный экран.
 */
interface AuthFeatureApi : FeatureApi {

    /**
     * Регистрирует экран авторизации для корневого графа.
     *
     * @param scope билдер `entryProvider` корневого графа.
     * @param onAuthSuccess вызывается после успешного входа; хост решает, куда уйти дальше.
     * @param modifier модификатор корня экрана.
     */
    fun registerRootEntries(
        scope: EntryProviderScope<NavKey>,
        onAuthSuccess: () -> Unit,
        modifier: Modifier = Modifier
    )
}
