package soft.divan.financemanager.uikit.model

import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarModel(
    val title: Int,
    val navigationIcon: ImageVector? = null,
    val navigationIconClick: () -> Unit = {},
    val actionIcon: ImageVector? = null,
    val actionIconClick: () -> Unit = {}
)
// Revue me>>
