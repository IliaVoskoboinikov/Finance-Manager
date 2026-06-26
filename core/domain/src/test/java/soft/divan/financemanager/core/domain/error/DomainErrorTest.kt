package soft.divan.financemanager.core.domain.error

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DomainErrorTest {

    @Test
    fun `singleton errors expose null cause`() {
        assertThat(DomainError.NoData.cause).isNull()
        assertThat(DomainError.Unauthorized.cause).isNull()
        assertThat(DomainError.OperationNotAllowed.cause).isNull()
        assertThat(DomainError.NetworkUnavailable.cause).isNull()
    }

    @Test
    fun `Unknown carries the provided cause`() {
        val throwable = IllegalStateException("boom")

        val error = DomainError.Unknown(throwable)

        assertThat(error.cause).isSameAs(throwable)
    }

    @Test
    fun `Unknown supports null cause`() {
        assertThat(DomainError.Unknown(null).cause).isNull()
    }

    @Test
    fun `domain errors are AppError instances`() {
        val error: AppError = DomainError.NoData

        assertThat(error).isInstanceOf(DomainError::class.java)
    }

    @Test
    fun `Unknown equality is based on cause`() {
        val throwable = RuntimeException("x")

        assertThat(DomainError.Unknown(throwable)).isEqualTo(DomainError.Unknown(throwable))
    }
}
