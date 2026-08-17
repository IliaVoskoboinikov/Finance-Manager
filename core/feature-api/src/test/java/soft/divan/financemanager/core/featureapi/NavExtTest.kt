package soft.divan.financemanager.core.featureapi

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class NavExtTest {

    private val featureApi = mockk<FeatureApi>()
    private val scope = mockk<EntryProviderScope<NavKey>>()
    private val navigator = mockk<Navigator>()

    /** Модификатор, отличимый от [Modifier] по умолчанию. */
    private object TestElement : Modifier.Element

    @Test
    fun `register delegates to featureApi registerEntries`() {
        val modifier: Modifier = TestElement
        justRun { featureApi.registerEntries(any(), any(), any()) }

        scope.register(featureApi, navigator, modifier)

        verify(exactly = 1) {
            featureApi.registerEntries(
                scope = scope,
                navigator = navigator,
                modifier = modifier
            )
        }
    }

    @Test
    fun `register uses empty modifier by default`() {
        justRun { featureApi.registerEntries(any(), any(), any()) }

        scope.register(featureApi, navigator)

        verify(exactly = 1) {
            featureApi.registerEntries(scope, navigator, Modifier)
        }
    }
}
