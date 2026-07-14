package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.domain.error.DomainError

class ErrorDataMaperTest {

    @Test
    fun `Network maps to NetworkUnavailable`() {
        assertThat(DataError.Network.toDomainError()).isEqualTo(DomainError.NetworkUnavailable)
    }

    @Test
    fun `Unauthorized maps to Unauthorized`() {
        assertThat(DataError.Unauthorized.toDomainError()).isEqualTo(DomainError.Unauthorized)
    }

    @Test
    fun `NotFound maps to NoData`() {
        assertThat(DataError.NotFound.toDomainError()).isEqualTo(DomainError.NoData)
    }

    @Test
    fun `GuestMode maps to GuestModeBlocked`() {
        assertThat(DataError.GuestMode.toDomainError()).isEqualTo(DomainError.GuestModeBlocked)
    }

    @Test
    fun `UnauthorizedBlocked maps to Unauthorized`() {
        assertThat(DataError.UnauthorizedBlocked.toDomainError()).isEqualTo(DomainError.Unauthorized)
    }

    @Test
    fun `Server maps to Unknown without cause`() {
        assertThat(DataError.Server.toDomainError()).isEqualTo(DomainError.Unknown(null))
    }

    @Test
    fun `LocalDb maps to Unknown preserving cause`() {
        val cause = IllegalStateException("db")

        val domainError = DataError.LocalDb(cause).toDomainError()

        assertThat(domainError).isEqualTo(DomainError.Unknown(cause))
    }

    @Test
    fun `Unknown maps to Unknown preserving cause`() {
        val cause = RuntimeException("boom")

        val domainError = DataError.Unknown(cause).toDomainError()

        assertThat(domainError).isEqualTo(DomainError.Unknown(cause))
    }

    @Test
    fun `Unknown with null cause maps to Unknown with null cause`() {
        assertThat(DataError.Unknown(null).toDomainError()).isEqualTo(DomainError.Unknown(null))
    }
}
