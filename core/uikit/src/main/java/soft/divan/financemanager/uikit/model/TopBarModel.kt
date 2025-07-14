package soft.divan.financemanager.uikit.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import soft.divan.financemanager.string.R
import soft.divan.financemanager.uikit.icons.ArrowConfirm
import soft.divan.financemanager.uikit.icons.Cross
import soft.divan.financemanager.uikit.icons.Pencil

open class TopBarModel(
    val title: Int,
    val navigationIcon: ImageVector? = null,
    val navigationIconClick: () -> Unit = {},
    val actionIcon: ImageVector? = null,
    val actionIconClick: () -> Unit = {},
) {


    data object AccountTopBar : TopBarModel(
        title = R.string.my_account,
        actionIcon = Icons.Filled.Pencil,
        actionIconClick = { /*navController ->*/

        }
    )


    data class AddAccountTopBar(val onConfirmClick: () -> Unit) : TopBarModel(
        title = R.string.my_account,
        navigationIcon = Icons.Filled.Cross,
        navigationIconClick = { /*navController ->
            navController.popBackStack()*/
        },

        actionIcon = Icons.Filled.ArrowConfirm,
        actionIconClick = { onConfirmClick() },
    )
}