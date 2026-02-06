package soft.divan.financemanager.core.data.util.coroutne

import kotlinx.coroutines.CoroutineScope

interface AppCoroutineContext {
    fun launch(block: suspend CoroutineScope.() -> Unit)
}
// Revue me>>
