package soft.divan.financemanager.presenter.util

import kotlinx.coroutines.flow.StateFlow

inline fun <reified T : Any, reified S : T> StateFlow<T>.ifSuccess(action: (S) -> Unit) {
    val state = this.value
    if (state is S) {
        action(state)
    }
}
