package soft.divan.financemanager.core.featureapi

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class NavExtTest {

    private val featureApi = mockk<FeatureApi>()
    private val navGraphBuilder = mockk<NavGraphBuilder>()
    private val navController = mockk<NavHostController>()
    private val scope = RouteScope("root")

    @Test
    fun `register delegates to featureApi registerGraph`() {
        val modifier = Modifier
        justRun { featureApi.registerGraph(any(), any(), any(), any()) }

        navGraphBuilder.register(featureApi, navController, scope, modifier)

        verify(exactly = 1) {
            featureApi.registerGraph(
                navGraphBuilder = navGraphBuilder,
                navController = navController,
                scope = scope,
                modifier = modifier
            )
        }
    }

    @Test
    fun `register uses empty modifier by default`() {
        justRun { featureApi.registerGraph(any(), any(), any(), any()) }

        navGraphBuilder.register(featureApi, navController, scope)

        verify(exactly = 1) {
            featureApi.registerGraph(navGraphBuilder, navController, scope, Modifier)
        }
    }
}
