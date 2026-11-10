package soft.divan.financemanager.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MainGraphViewModelTest {

    private val probeFactory = viewModelFactory { initializer { ProbeViewModel() } }

    @Test
    fun `discard clears the graph view models and the next request creates new ones`() {
        val graph = MainGraphViewModel()
        val before = probeIn(graph)

        graph.discard(generation = 1)

        assertThat(before.cleared).isTrue()
        assertThat(probeIn(graph)).isNotSameAs(before)
    }

    @Test
    fun `graph view models live as long as the graph store is not discarded`() {
        val graph = MainGraphViewModel()

        assertThat(probeIn(graph)).isSameAs(probeIn(graph))
    }

    @Test
    fun `finishing the activity clears the graph view models`() {
        val activityStore = ViewModelStore()
        val graph = ViewModelProvider.create(
            activityStore,
            viewModelFactory { initializer { MainGraphViewModel() } }
        )[MainGraphViewModel::class]
        val probe = probeIn(graph)

        activityStore.clear()

        assertThat(probe.cleared).isTrue()
    }

    @Test
    fun `graph not shown yet in this process is fresh until the first wipe`() {
        val graph = MainGraphViewModel()

        assertThat(graph.isStale(generation = 0, staysShown = true)).isFalse()
        // Стек из Bundle прошлого процесса относится к данным со старта — после стирания он чужой
        assertThat(graph.isStale(generation = 1, staysShown = true)).isTrue()
    }

    @Test
    fun `hidden graph becomes stale when the data generation changes`() {
        val graph = MainGraphViewModel()
        graph.onShown(generation = 0)
        graph.onHidden()

        assertThat(graph.isStale(generation = 0, staysShown = true)).isFalse()
        assertThat(graph.isStale(generation = 1, staysShown = true)).isTrue()
        assertThat(graph.isStale(generation = 1, staysShown = false)).isTrue()
    }

    @Test
    fun `graph that stays on screen is not stale — it handles the wipe itself`() {
        val graph = MainGraphViewModel()
        graph.onShown(generation = 0)

        assertThat(graph.isStale(generation = 1, staysShown = true)).isFalse()
    }

    @Test
    fun `graph hidden in the frame of a wipe is stale at once`() {
        val graph = MainGraphViewModel()
        // Ещё на экране: Activity была в фоне и не перерисовывалась, пока шло стирание
        graph.onShown(generation = 0)

        assertThat(graph.isStale(generation = 1, staysShown = false)).isTrue()
    }

    @Test
    fun `discarded graph belongs to the current generation`() {
        val graph = MainGraphViewModel()
        graph.onShown(generation = 0)
        graph.onHidden()

        graph.discard(generation = 1)

        assertThat(graph.isStale(generation = 1, staysShown = true)).isFalse()
        assertThat(graph.isStale(generation = 2, staysShown = true)).isTrue()
    }

    private fun probeIn(graph: MainGraphViewModel): ProbeViewModel =
        ViewModelProvider.create(graph, probeFactory)[ProbeViewModel::class]

    private class ProbeViewModel : ViewModel() {
        var cleared = false
            private set

        override fun onCleared() {
            cleared = true
        }
    }
}
