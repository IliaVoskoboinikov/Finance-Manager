package soft.divan.financemanager.core.data.error

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.AppError

class DataErrorTest {

    @Test
    fun `singleton errors expose null cause`() {
        assertThat(DataError.Network.cause).isNull()
        assertThat(DataError.Unauthorized.cause).isNull()
        assertThat(DataError.Server.cause).isNull()
        assertThat(DataError.NotFound.cause).isNull()
        assertThat(DataError.GuestMode.cause).isNull()
        assertThat(DataError.UnauthorizedBlocked.cause).isNull()
    }

    @Test
    fun `LocalDb carries the provided cause`() {
        val throwable = IllegalStateException("db is broken")

        assertThat(DataError.LocalDb(throwable).cause).isSameAs(throwable)
        assertThat(DataError.LocalDb(null).cause).isNull()
    }

    @Test
    fun `Unknown carries the provided cause`() {
        val throwable = RuntimeException("boom")

        assertThat(DataError.Unknown(throwable).cause).isSameAs(throwable)
        assertThat(DataError.Unknown(null).cause).isNull()
    }

    @Test
    fun `data errors are AppError instances`() {
        val error: AppError = DataError.Network

        assertThat(error).isInstanceOf(DataError::class.java)
    }

    @Test
    fun `data class errors use structural equality`() {
        val throwable = RuntimeException("x")

        assertThat(DataError.LocalDb(throwable)).isEqualTo(DataError.LocalDb(throwable))
        assertThat(DataError.Unknown(throwable)).isEqualTo(DataError.Unknown(throwable))
    }
}
