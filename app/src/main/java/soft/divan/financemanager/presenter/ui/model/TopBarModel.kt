package soft.divan.financemanager.presenter.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.icons.ArrowBack
import soft.divan.financemanager.presenter.ui.icons.ArrowConfirm
import soft.divan.financemanager.presenter.ui.icons.Clock
import soft.divan.financemanager.presenter.ui.icons.Cross
import soft.divan.financemanager.presenter.ui.icons.Pencil
import soft.divan.financemanager.presenter.ui.icons.TabletWatch
import soft.divan.financemanager.presenter.ui.screens.HistoryExpensesScreen
import soft.divan.financemanager.presenter.ui.screens.HistoryIncomeScreen

sealed class TopBarModel(
    val title: Int,
    val navigationIcon: ImageVector? = null,
    val navigationIconClick: (NavHostController) -> Unit = {},
    val actionIcon: ImageVector? = null,
    val actionIconClick: (NavHostController) -> Unit = {},
) {
    data object ExpansesTopBar : TopBarModel(
        title = R.string.expanses_today,
        actionIcon = Icons.Filled.Clock,
        actionIconClick = { navController ->
            navController.navigate(HistoryExpensesScreen.route)
        }
    )

    data object IncomeTopBar : TopBarModel(
        title = R.string.income_today,
        actionIcon = Icons.Filled.Clock,
        actionIconClick = { navController ->
            navController.navigate(HistoryIncomeScreen.route)
        }
    )

    data object AccountTopBar : TopBarModel(
        title = R.string.my_account,
        actionIcon = Icons.Filled.Pencil,
        actionIconClick = { navController ->

        }
    )

    data object ArticlesTopBar : TopBarModel(
        title = R.string.my_articles
    )

    data object SettingsTopBar : TopBarModel(
        title = R.string.settings
    )

    data object HistoryTopBar : TopBarModel(
        title = R.string.my_history,
        navigationIcon = Icons.Filled.ArrowBack,
        navigationIconClick = { navController ->
            navController.popBackStack()
        },
        actionIcon = Icons.Filled.TabletWatch,
        actionIconClick = { navController ->

        }
    )

    data class AddAccountTopBar( val onConfirmClick: () -> Unit) : TopBarModel(
        title = R.string.my_account,
        navigationIcon = Icons.Filled.Cross,
        navigationIconClick = { navController ->
            navController.popBackStack()
        },

        actionIcon = Icons.Filled.ArrowConfirm,
        actionIconClick = { onConfirmClick() },
    )
}
