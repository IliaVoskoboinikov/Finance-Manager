package soft.divan.financemanager.core.auth.data.provider

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.data.source.TokenLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.model.SessionState
import soft.divan.financemanager.core.database.util.DatabaseCleanupManager

@OptIn(ExperimentalCoroutinesApi::class)
class AuthStateHolderTest {

    private val sessionDataSource = mockk<SessionLocalDataSource>(relaxed = true)
    private val tokenDataSource = mockk<TokenLocalDataSource>(relaxed = true)
    private val dbCleanupManager = mockk<DatabaseCleanupManager>(relaxed = true)
    private val testScope = TestScope()

    private lateinit var authStateHolder: AuthStateHolder

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0

        every { sessionDataSource.getAuthStatus() } returns flowOf(AuthStatus.UNAUTHORIZED)
        every { tokenDataSource.getAccessToken() } returns flowOf(null)
        every { tokenDataSource.getRefreshToken() } returns flowOf(null)
    }

    private fun createHolder() {
        authStateHolder = AuthStateHolder(
            sessionDataSource,
            tokenDataSource,
            dbCleanupManager,
            testScope
        )
    }

    @Test
    fun `init should restore state from dataSources`() = runTest {
        every { sessionDataSource.getAuthStatus() } returns flowOf(AuthStatus.AUTHORIZED)
        every { tokenDataSource.getAccessToken() } returns flowOf("access")
        every { tokenDataSource.getRefreshToken() } returns flowOf("refresh")

        createHolder()
        testScope.runCurrent()

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.AUTHORIZED)
        assertThat(
            authStateHolder.currentSessionState()
        ).isInstanceOf(SessionState.Authorized::class.java)
    }

    @Test
    fun `observeStatus emits restored status after initialization`() = runTest {
        every { sessionDataSource.getAuthStatus() } returns flowOf(AuthStatus.GUEST)

        createHolder()
        testScope.runCurrent()

        assertThat(authStateHolder.observeStatus().first()).isEqualTo(AuthStatus.GUEST)
    }

    @Test
    fun `sendEvent OnLoginSuccess should update state and persistence`() = runTest {
        createHolder()
        testScope.runCurrent()

        val event = AuthEvent.OnLoginSuccess("new_access", "new_refresh", shouldMergeData = true)
        authStateHolder.sendEvent(event)

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.AUTHORIZED)
        val state = authStateHolder.currentSessionState() as SessionState.Authorized
        assertThat(state.accessToken).isEqualTo("new_access")

        coVerify {
            tokenDataSource.updateAccessToken("new_access")
            tokenDataSource.updateRefreshToken("new_refresh")
            sessionDataSource.setAuthStatus(AuthStatus.AUTHORIZED)
        }
    }

    @Test
    fun `sendEvent OnLogout with clearData should clear everything`() = runTest {
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(AuthEvent.OnLogout(shouldClearData = true))

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.UNAUTHORIZED)
        coVerify {
            tokenDataSource.clearTokens()
            dbCleanupManager.clearUserData()
            sessionDataSource.setAuthStatus(AuthStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun `sendEvent OnSessionExpired should clear everything`() = runTest {
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(AuthEvent.OnSessionExpired)

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.UNAUTHORIZED)
        coVerify {
            tokenDataSource.clearTokens()
            dbCleanupManager.clearUserData()
            sessionDataSource.setAuthStatus(AuthStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun `login from guest without merge wipes local data first`() = runTest {
        every { sessionDataSource.getAuthStatus() } returns flowOf(AuthStatus.GUEST)
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(
            AuthEvent.OnLoginSuccess("access", "refresh", shouldMergeData = false)
        )

        coVerify { dbCleanupManager.clearUserData() }
        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.AUTHORIZED)
    }

    @Test
    fun `login from guest with merge keeps local data`() = runTest {
        every { sessionDataSource.getAuthStatus() } returns flowOf(AuthStatus.GUEST)
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(
            AuthEvent.OnLoginSuccess("access", "refresh", shouldMergeData = true)
        )

        coVerify(exactly = 0) { dbCleanupManager.clearUserData() }
        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.AUTHORIZED)
    }

    @Test
    fun `logout without clearData switches to guest and keeps db`() = runTest {
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(AuthEvent.OnLogout(shouldClearData = false))

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.GUEST)
        coVerify {
            tokenDataSource.clearTokens()
            sessionDataSource.setAuthStatus(AuthStatus.GUEST)
        }
        coVerify(exactly = 0) { dbCleanupManager.clearUserData() }
    }

    @Test
    fun `enter as guest clears tokens but keeps data`() = runTest {
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(AuthEvent.OnEnterAsGuest)

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.GUEST)
        coVerify {
            tokenDataSource.clearTokens()
            sessionDataSource.setAuthStatus(AuthStatus.GUEST)
        }
        coVerify(exactly = 0) { dbCleanupManager.clearUserData() }
    }

    @Test
    fun `clear data event wipes db without changing status`() = runTest {
        createHolder()
        testScope.runCurrent()

        authStateHolder.sendEvent(AuthEvent.OnClearData)

        coVerify { dbCleanupManager.clearUserData() }
        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.UNAUTHORIZED)
    }

    @Test
    fun `restore maps authorized status with missing tokens to unauthorized`() = runTest {
        every { sessionDataSource.getAuthStatus() } returns flowOf(AuthStatus.AUTHORIZED)
        every { tokenDataSource.getAccessToken() } returns flowOf(null)
        every { tokenDataSource.getRefreshToken() } returns flowOf(null)

        createHolder()
        testScope.runCurrent()

        assertThat(authStateHolder.currentStatus()).isEqualTo(AuthStatus.UNAUTHORIZED)
        assertThat(authStateHolder.currentSessionState())
            .isEqualTo(SessionState.Unauthorized)
    }
}
