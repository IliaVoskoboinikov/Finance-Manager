package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Реализация интерфейса [TransactionRepository] для работы с транзакциями через удаленный источник данных.
 *
 * Класс использует [soft.divan.financemanager.data.source.TransactionRemoteDataSource] для получения транзакций по аккаунту и периоду времени
 * из удаленного API. Результаты возвращаются в виде [Flow], что обеспечивает поддержку асинхронного и реактивного подхода.
 *
 * @property transactionRemoteDataSource удаленный источник данных для транзакций
 *
 * @see TransactionRepository
 * @see soft.divan.financemanager.data.source.TransactionRemoteDataSource
 */
class TransactionRepositoryImp @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource
) : TransactionRepository {


    /**
     * Получает список транзакций по заданному идентификатору аккаунта и периоду времени.
     *
     * Метод выполняет запрос к удаленному API с параметрами:
     * - [accountId] — идентификатор аккаунта, по которому нужно получить транзакции,
     * - [startDate] — необязательная дата начала периода в формате строки (например, ISO 8601),
     * - [endDate] — необязательная дата окончания периода.
     *
     * Полученные данные преобразуются из DTO в сущности, затем в доменные модели [Transaction],
     * которые эмитируются через [Flow].
     *
     * @param accountId идентификатор аккаунта для выборки транзакций
     * @param startDate (опционально) дата начала периода фильтрации транзакций, может быть null
     * @param endDate (опционально) дата окончания периода фильтрации транзакций, может быть null
     * @return [Flow] со списком доменных моделей [Transaction], соответствующих фильтру
     */

    override suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<Transaction>> = flow {
        val response = transactionRemoteDataSource.getTransactionsByAccountAndPeriod(
            accountId, startDate, endDate
        )
        val transactionsByAccountAndPeriodDto = response.body().orEmpty()
        val transactionsByAccountAndPeriodEntity =
            transactionsByAccountAndPeriodDto.map { it.toEntity() }
        val transactionsByAccountAndPeriod = transactionsByAccountAndPeriodEntity.map { it.toDomain() }
        emit(transactionsByAccountAndPeriod)

    }
}