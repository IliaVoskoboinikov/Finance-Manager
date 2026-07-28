package soft.divan.financemanager.core.data.util.safeCall

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import kotlin.coroutines.EmptyCoroutineContext

class SafeDbCallTest {

    private val errorLogger: ErrorLogger = mockk(relaxed = true)

    /* ---------- safeDbCall ---------- */

    @Test
    fun `safeDbCall returns Success with call result`() = runTest {
        val result = safeDbCall(errorLogger, EmptyCoroutineContext) { 42 }

        assertThat(result).isEqualTo(DomainResult.Success(42))
    }

    @Test
    fun `safeDbCall supports null result`() = runTest {
        val result = safeDbCall<String?>(errorLogger, EmptyCoroutineContext) { null }

        assertThat(result).isEqualTo(DomainResult.Success(null))
    }

    @Test
    fun `safeDbCall maps exception to Unknown with cause and logs it`() = runTest {
        val boom = IllegalStateException("db down")

        val result = safeDbCall(errorLogger, EmptyCoroutineContext) { throw boom }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
        verify { errorLogger.recordError(boom) }
    }

    /* ---------- safeDbFlow ---------- */

    @Test
    fun `safeDbFlow wraps every emission in Success`() = runTest {
        val emissions = safeDbFlow(errorLogger, EmptyCoroutineContext) {
            flowOf("a", "b")
        }.toList()

        assertThat(emissions).containsExactly(
            DomainResult.Success("a"),
            DomainResult.Success("b")
        )
    }

    @Test
    fun `safeDbFlow emits Failure when upstream flow throws and logs error`() = runTest {
        val boom = RuntimeException("query failed")

        val emissions = safeDbFlow<String>(errorLogger, EmptyCoroutineContext) {
            flow { throw boom }
        }.toList()

        assertThat(emissions).containsExactly(DomainResult.Failure(DomainError.Unknown(boom)))
        verify { errorLogger.recordError(boom) }
    }

    @Test
    fun `safeDbFlow emits successful values before upstream error`() = runTest {
        val boom = RuntimeException("late failure")

        val emissions = safeDbFlow(errorLogger, EmptyCoroutineContext) {
            flow {
                emit("ok")
                throw boom
            }
        }.toList()

        assertThat(emissions).containsExactly(
            DomainResult.Success("ok"),
            DomainResult.Failure(DomainError.Unknown(boom))
        )
    }

    @Test
    fun `safeDbFlow does not wrap exceptions thrown while creating the flow`() = runTest {
        // catch навешивается на flow из block(), поэтому исключение самого block()
        // не превращается в Failure, а доходит до коллектора как есть
        val boom = IllegalArgumentException("no dao")

        val thrown = runCatching {
            safeDbFlow<String>(errorLogger, EmptyCoroutineContext) { throw boom }.toList()
        }.exceptionOrNull()

        assertThat(thrown).isSameAs(boom)
    }

    @Test
    fun `safeDbFlow emits nothing for empty upstream`() = runTest {
        val emissions = safeDbFlow(errorLogger, EmptyCoroutineContext) {
            flowOf<String>()
        }.toList()

        assertThat(emissions).isEmpty()
    }
}
