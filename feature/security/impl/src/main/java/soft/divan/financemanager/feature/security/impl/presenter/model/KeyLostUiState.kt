package soft.divan.financemanager.feature.security.impl.presenter.model

import androidx.compose.runtime.Immutable

/**
 * Состояние экрана «ключ данных утерян».
 *
 * @property isGuest Данные только на устройстве — после стирания их не вернуть.
 * @property showConfirm Показано предупреждение перед стиранием.
 * @property inProgress Идёт повторная попытка или стирание.
 */
@Immutable
data class KeyLostUiState(
    val isGuest: Boolean = false,
    val showConfirm: Boolean = false,
    val inProgress: Boolean = false
)
