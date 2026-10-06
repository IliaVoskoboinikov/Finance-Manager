package soft.divan.financemanager.feature.security.impl.domain.usecase

/**
 * Выход из состояния «ключ утерян»: [retry] — прочитать ключи ещё раз, [recover] — стереть
 * недоступные данные и начать заново.
 */
interface RecoverLocalDataUseCase {

    /** Повторная попытка прочитать ключи — на случай временного сбоя. */
    suspend fun retry()

    /** Стереть недоступные данные; под PIN — вместе с выходом из аккаунта и снятием PIN. */
    suspend fun recover()
}
