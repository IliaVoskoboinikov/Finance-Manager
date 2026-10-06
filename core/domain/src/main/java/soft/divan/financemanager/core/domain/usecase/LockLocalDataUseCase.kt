package soft.divan.financemanager.core.domain.usecase

/**
 * Закрывает базу, если уровень защиты требует PIN; на остальных уровнях ничего не делает.
 *
 * Вызывается, когда приложение надолго ушло в фон: ключ базы не должен жить в памяти дольше,
 * чем пользователь им пользуется.
 */
interface LockLocalDataUseCase {
    suspend operator fun invoke()
}
