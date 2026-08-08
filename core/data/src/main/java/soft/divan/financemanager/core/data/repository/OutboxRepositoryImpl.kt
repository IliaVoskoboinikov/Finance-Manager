package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
import soft.divan.financemanager.core.domain.repository.OutboxRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

/**
 * Доступ к состоянию очереди исходящих операций для presentation-слоя.
 *
 * Наружу отдаётся только то, что имеет смысл для пользователя: сколько изменений не доехало и
 * команда «повторить». Внутреннее устройство очереди (статусы, попытки, снимки запросов) остаётся
 * в data-слое.
 */
class OutboxRepositoryImpl @Inject constructor(
    private val localDataSource: OutboxLocalDataSource,
    private val processor: OutboxProcessor,
    private val errorLogger: ErrorLogger
) : OutboxRepository {

    override fun observeFailedCount(): Flow<DomainResult<Int>> =
        safeDbFlow(errorLogger) { localDataSource.observeFailedCount() }

    /**
     * Возвращает отложенные операции в очередь и сразу пробует их отправить.
     *
     * Прогон выполняется здесь же, а не откладывается до следующего фонового синка: пользователь
     * нажал «повторить» и вправе увидеть результат сразу.
     */
    override suspend fun retryFailed(): DomainResult<Unit> =
        safeDbCall(errorLogger) {
            localDataSource.requeueFailed(System.currentTimeMillis())
            processor.process()
            Unit
        }
}
