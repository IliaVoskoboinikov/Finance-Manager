package soft.divan.financemanager.feature.security.impl.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

/** Сохраняет PIN, придуманный на экране создания (PIN ещё не задан): хеш, зашифрованный KeyStore. */
interface SavePinUseCase {

    /** @return `Failure(Unknown)` — KeyStore отказал в шифровании, PIN не сохранён; можно повторить. */
    suspend operator fun invoke(pin: String): DomainResult<Unit>
}
