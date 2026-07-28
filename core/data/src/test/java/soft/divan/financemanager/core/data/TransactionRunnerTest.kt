package soft.divan.financemanager.core.data

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult

class TransactionRunnerTest {

    @Test
    fun `rollbackOnError returns data for Success`() {
        val result: DomainResult<Int> = DomainResult.Success(42)

        assertThat(result.rollbackOnError()).isEqualTo(42)
    }

    @Test
    fun `rollbackOnError throws TransactionRollbackException for Failure`() {
        val result: DomainResult<Int> = DomainResult.Failure(DomainError.NoData)

        assertThatThrownBy { result.rollbackOnError() }
            .isInstanceOf(TransactionRollbackException::class.java)
            .matches { (it as TransactionRollbackException).error == DomainError.NoData }
    }

    @Test
    fun `TransactionRollbackException carries the domain error`() {
        val error = DomainError.OperationNotAllowed

        assertThat(TransactionRollbackException(error).error).isEqualTo(error)
    }
}
