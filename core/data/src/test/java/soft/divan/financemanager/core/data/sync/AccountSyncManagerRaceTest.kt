package soft.divan.financemanager.core.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.impl.AccountSyncManagerImpl
import soft.divan.financemanager.core.loggingerror.ErrorLogger

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSyncManagerRaceTest {

    private val remoteDataSource = mockk<AccountRemoteDataSource>()
    private val localDataSource = mockk<AccountLocalDataSource>()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private lateinit var syncManager: AccountSyncManagerImpl

    @Before
    fun setup() {
        syncManager = AccountSyncManagerImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            errorLogger = errorLogger
        )
    }

    @Test
    fun `pullServerData should not create duplicate with mutex`() = runTest {
        val dto = AccountDto(
            id = 1,
            userId = 1,
            name = "Test",
            balance = "100",
            currency = "USD",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        // возвращаем один аккаунт с сервера
        coEvery { remoteDataSource.getAll() } returns Response.success(listOf(dto))

        // локально нет аккаунтов
        coEvery { localDataSource.getByServerIds(any()) } coAnswers {
            delay(50) // симулируем гонку
            emptyList()
        }

        // имитация сохранения в БД
        coEvery { localDataSource.create(any()) } coAnswers {
            delay(50) // чтобы имитировать реальную задержку записи
        }

        // Запускаем два параллельных pullServerData
        coroutineScope {
            launch { syncManager.pullServerData() }
            launch { syncManager.pullServerData() }
        }

        // Проверяем: локальная запись создалась только один раз
        coVerify(exactly = 1) { localDataSource.create(any()) }
    }
}
