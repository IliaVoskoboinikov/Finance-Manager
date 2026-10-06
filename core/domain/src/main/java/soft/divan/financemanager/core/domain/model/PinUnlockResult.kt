package soft.divan.financemanager.core.domain.model

/** Итог попытки открыть данные по PIN. */
sealed interface PinUnlockResult {

    /** PIN верный, база открыта. */
    data object Success : PinUnlockResult

    /** Уровень защиты не требует PIN для данных — проверять PIN нужно обычным способом. */
    data object NotRequired : PinUnlockResult

    /** PIN неверный; ошибка учтена. */
    data class WrongPin(val lockout: PinLockout) : PinUnlockResult

    /** Ввод запрещён до [PinLockout.lockedUntil]; попытка не проверялась и не учтена. */
    data class LockedOut(val lockout: PinLockout) : PinUnlockResult

    /** Попытки исчерпаны: данные и ключи стёрты, выполнен выход из аккаунта. */
    data object Wiped : PinUnlockResult

    /** Ключ базы недоступен — нужен экран восстановления. */
    data object KeyLost : PinUnlockResult
}

/** Итог смены уровня защиты или PIN. */
sealed interface ProtectionChangeResult {

    /** Изменение сохранено. */
    data object Changed : ProtectionChangeResult

    /** PIN для подтверждения неверный; ничего не изменилось. */
    data object WrongPin : ProtectionChangeResult

    /**
     * Не все изменения отправлены на сервер, поэтому уровень [DataProtectionLevel.PIN] не включён:
     * на нём нет фоновой синхронизации, и неотправленное зависло бы надолго.
     */
    data object UnsentChanges : ProtectionChangeResult

    /** Операция невозможна: ключ недоступен или хранилище ключей отказало. */
    data object Failed : ProtectionChangeResult
}
