package soft.divan.financemanager.feature.auth.api

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.core.featureapi.RouteScope

interface AuthFeatureApi : FeatureApi {
    val profileRoute: String

    // Добавляем версию без scope для RootNavGraph (если нужно сохранить совместимость)
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier = Modifier,
        onFinish: () -> Unit
    )
}
