package soft.divan.financemanager.uikit.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Описание верхней панели экрана для [soft.divan.financemanager.uikit.components.TopBar].
 *
 * @param isActionEnabled активна ли кнопка действия. Экран выключает её, пока действие
 * выполняется (например, идёт сохранение), чтобы повторный тап не был похож на рабочий.
 */
data class TopBarModel(
    val title: Int,
    val navigationIcon: ImageVector? = null,
    val navigationIconClick: () -> Unit = {},
    val actionIcon: ImageVector? = null,
    val actionIconClick: () -> Unit = {},
    val isActionEnabled: Boolean = true
)
