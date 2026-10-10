package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountStatus
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus

fun AccountDto.toEntity(localId: String, syncStatus: SyncStatus): AccountEntity = AccountEntity(
    localId = localId,
    serverId = id,
    name = name,
    balance = balance.toPlainString(),
    currencyId = currencyId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    status = AccountStatus.fromWire(status).name
)

fun Account.toEntity(serverId: String?, syncStatus: SyncStatus): AccountEntity = AccountEntity(
    localId = id,
    serverId = serverId,
    name = name,
    balance = balance.toPlainString(),
    currencyId = currencyId,
    createdAt = TimeMapper.toApi(createdAt),
    updatedAt = TimeMapper.toApi(updatedAt),
    syncStatus = syncStatus,
    status = status.name
)

fun AccountEntity.toDomain(): Account = Account(
    id = localId,
    name = name,
    balance = balance.toBigDecimal(),
    currencyId = currencyId,
    createdAt = TimeMapper.fromApi(createdAt),
    updatedAt = TimeMapper.fromApi(updatedAt),
    status = AccountStatus.fromWire(status)
)

/**
 * Запрос на создание счёта (`POST /account`) из доменной модели.
 *
 * `id = id` — стабильный клиентский UUID как идемпотентный ключ: сервер принимает клиентский id
 * и на повтор с тем же id отбивает дубль по первичному ключу (защита от двойного создания при
 * потере ACK и ретраях).
 */
fun Account.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    id = id,
    name = name,
    balance = balance,
    currencyId = currencyId
)

fun AccountEntity.toUpdateDto(): UpdateAccountRequestDto = UpdateAccountRequestDto(
    name = name,
    balance = balance.toBigDecimal(),
    currencyId = currencyId
)

/**
 * Запрос на создание счёта из локальной сущности (push pending-записи).
 *
 * `id = localId` — стабильный клиентский UUID как идемпотентный ключ (см. [Account.toDto]):
 * сервер дедуплицирует повтор по первичному ключу.
 */
fun AccountEntity.toDto(): CreateAccountRequestDto = CreateAccountRequestDto(
    id = localId,
    name = name,
    balance = balance.toBigDecimal(),
    currencyId = currencyId
)
