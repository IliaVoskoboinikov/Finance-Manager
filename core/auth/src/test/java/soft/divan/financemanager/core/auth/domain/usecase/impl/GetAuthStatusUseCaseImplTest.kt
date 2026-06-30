package soft.divan.financemanager.core.auth.domain.usecase.impl

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider

class GetAuthStatusUseCaseImplTest {

    private val authStateProvider = mockk<AuthStateProvider>()
    private lateinit var useCase: GetAuthStatusUseCaseImpl

    @Before
    fun setup() {
        useCase = GetAuthStatusUseCaseImpl(authStateProvider)
    }

    @Test
    fun `invoke should return status from state provider`() = runTest {
        every { authStateProvider.observeStatus() } returns flowOf(AuthStatus.AUTHORIZED)

        val status = useCase().first()

        assertThat(status).isEqualTo(AuthStatus.AUTHORIZED)
    }
}
