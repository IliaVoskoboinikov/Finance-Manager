package soft.divan.financemanager.core.data.util.coroutne.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import soft.divan.common.di.ApplicationScope
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext

class DefaultAppCoroutineContext(
    @param:ApplicationScope val scope: CoroutineScope,
    @param:IoDispatcher val dispatcher: CoroutineDispatcher,
    val exceptionHandler: CoroutineExceptionHandler
) : AppCoroutineContext {

    override fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(dispatcher + exceptionHandler) {
            block()
        }
    }
}
// Revue me>>
