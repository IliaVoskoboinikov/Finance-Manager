package soft.divan.finansemanager.account.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import soft.divan.financemanager.core.data.toAccountBriefDomain
import soft.divan.financemanager.core.data.toDomain
import soft.divan.financemanager.core.data.toEntity
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.model.CreateAccountRequest
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.network.dto.CreateAccountRequestDto
import soft.divan.finansemanager.account.data.sourse.AccountRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация интерфейса [AccountRepository] для работы с аккаунтами через удаленный источник данных.
 *
 * Класс использует [soft.divan.financemanager.data.source.AccountRemoteDataSource] для получения, создания и обновления данных аккаунтов
 * из удаленного API. Все методы возвращают результаты в виде [kotlinx.coroutines.flow.Flow], что позволяет использовать
 * асинхронное и реактивное программирование.
 *
 * @property accountRemoteDataSource источник удаленных данных для аккаунтов
 *
 * @see AccountRepository
 * @see soft.divan.financemanager.data.source.AccountRemoteDataSource
 */
@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource
) : AccountRepository {

    /**
     * Получает список всех аккаунтов пользователя.
     *
     * Метод запрашивает данные аккаунтов у удаленного источника,
     * преобразует DTO-объекты в сущности (Entity) (Для будущей бд), затем в доменные модели,
     * и эмитирует полученный список через [kotlinx.coroutines.flow.Flow].
     *
     * @return [kotlinx.coroutines.flow.Flow] со списком [soft.divan.financemanager.domain.model.Account] — доменных моделей аккаунтов.
     */
    override fun getAccounts(): Flow<List<Account>> = flow {
        val response = accountRemoteDataSource.getAccounts()
        val accountDto = response.body().orEmpty()
        val accountsEntity = accountDto.map { it.toEntity() }
        val accounts = accountsEntity.map { it.toDomain() }
        emit(accounts)

    }


    /**
     * Создает новый аккаунт с заданными параметрами.
     *
     * Принимает доменную модель [soft.divan.financemanager.domain.model.CreateAccountRequest], преобразует её в DTO,
     * отправляет запрос на создание аккаунта к удаленному API,
     * затем преобразует ответ обратно в доменную модель и эмитирует её через [Flow].
     *
     * @param createAccountRequest доменная модель с параметрами нового аккаунта (имя, баланс, валюта)
     * @return [Flow] с созданным [soft.divan.financemanager.domain.model.Account] — доменной моделью аккаунта.
     */

    override fun createAccount(createAccountRequest: CreateAccountRequest): Flow<Account> = flow {
        val requestDto = CreateAccountRequestDto(
            name = createAccountRequest.name,
            balance = createAccountRequest.balance.toPlainString(),
            currency = createAccountRequest.currency
        )
        val response = accountRemoteDataSource.createAccount(requestDto)
        val accountDto = response.body()!!
        val accountsEntity = accountDto.toEntity()
        val accounts = accountsEntity.toDomain()
        emit(accounts)


    }

    /**
     * Обновляет краткую информацию об аккаунте.
     *
     * Принимает доменную модель [soft.divan.financemanager.domain.model.AccountBrief], отправляет запрос на обновление данных
     * на удаленный сервер, получает обновленные данные и эмитирует их через [Flow].
     *
     * @param accountBrief доменная модель с краткой информацией об аккаунте для обновления
     * @return [Flow] с обновленной моделью [soft.divan.financemanager.domain.model.AccountBrief].
     */

    override fun updateAccount(accountBrief: AccountBrief): Flow<AccountBrief> = flow {
        val response = accountRemoteDataSource.updateAccount(accountBrief)
        val accountWithStatsDto = response.body()!!
        val accountBrief = accountWithStatsDto.toAccountBriefDomain()
        emit(accountBrief)
    }

}