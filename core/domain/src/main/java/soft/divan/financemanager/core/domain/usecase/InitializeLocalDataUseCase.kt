package soft.divan.financemanager.core.domain.usecase

/**
 * Читает ключи шифрования и открывает базу, если это можно сделать без пользователя.
 *
 * Вызывается при старте процесса, до любого обращения к данным.
 */
interface InitializeLocalDataUseCase {
    suspend operator fun invoke()
}
