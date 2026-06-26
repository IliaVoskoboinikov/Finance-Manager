package soft.divan.financemanager.core.domain.result

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError

class DomainResultTest {

    @Test
    fun `fold invokes onSuccess for Success`() {
        val result: DomainResult<Int> = DomainResult.Success(42)

        val folded = result.fold(
            onSuccess = { "value=$it" },
            onFailure = { "error" }
        )

        assertThat(folded).isEqualTo("value=42")
    }

    @Test
    fun `fold invokes onFailure for Failure`() {
        val result: DomainResult<Int> = DomainResult.Failure(DomainError.NoData)

        val folded = result.fold(
            onSuccess = { "value" },
            onFailure = { "error=$it" }
        )

        assertThat(folded).isEqualTo("error=${DomainError.NoData}")
    }

    @Test
    fun `onSuccess runs action and returns same instance for Success`() {
        val result: DomainResult<String> = DomainResult.Success("data")
        var captured: String? = null

        val returned = result.onSuccess { captured = it }

        assertThat(captured).isEqualTo("data")
        assertThat(returned).isSameAs(result)
    }

    @Test
    fun `onSuccess does not run action for Failure`() {
        val result: DomainResult<String> = DomainResult.Failure(DomainError.Unauthorized)
        var invoked = false

        val returned = result.onSuccess { invoked = true }

        assertThat(invoked).isFalse()
        assertThat(returned).isSameAs(result)
    }

    @Test
    fun `onFailure runs action and returns same instance for Failure`() {
        val error = DomainError.NetworkUnavailable
        val result: DomainResult<String> = DomainResult.Failure(error)
        var captured: DomainError? = null

        val returned = result.onFailure { captured = it }

        assertThat(captured).isEqualTo(error)
        assertThat(returned).isSameAs(result)
    }

    @Test
    fun `onFailure does not run action for Success`() {
        val result: DomainResult<String> = DomainResult.Success("data")
        var invoked = false

        val returned = result.onFailure { invoked = true }

        assertThat(invoked).isFalse()
        assertThat(returned).isSameAs(result)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val result: DomainResult<Int> = DomainResult.Success(7)

        assertThat(result.getOrNull()).isEqualTo(7)
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        val result: DomainResult<Int> = DomainResult.Failure(DomainError.OperationNotAllowed)

        assertThat(result.getOrNull()).isNull()
    }
}
