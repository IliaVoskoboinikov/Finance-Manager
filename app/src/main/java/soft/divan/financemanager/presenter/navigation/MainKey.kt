package soft.divan.financemanager.presenter.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Главный экран приложения (оболочка с нижней навигацией) в корневом back stack.
 *
 * Принадлежит `app`, а не фиче: за ним стоит не один экран, а весь граф вкладок.
 */
@Serializable
internal data object MainKey : NavKey
