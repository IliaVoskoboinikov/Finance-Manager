package soft.divan.financemanager.feature.security.impl.presenter.model

/** Шаг создания PIN: придумать или повторить. */
enum class CreatePinStep { ENTER, CONFIRM }

/** Почему ввод начат заново. */
enum class CreatePinError {
    /** Повтор не совпал с первым вводом. */
    MISMATCH,

    /** KeyStore не смог зашифровать PIN. */
    SAVE_FAILED
}

/**
 * Состояние экрана создания PIN.
 *
 * Сам первый PIN сюда не попадает — его держит ViewModel до повтора.
 *
 * @property error почему ввод начат заново; `null` — ошибки нет.
 * @property isSaving PIN хешируется и сохраняется, ввод закрыт.
 * @property isCompleted PIN сохранён — экран закрывается и больше не принимает ввод.
 */
data class CreatePinUiState(
    val step: CreatePinStep = CreatePinStep.ENTER,
    val error: CreatePinError? = null,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false
) {
    /** Ввод разрешён: не идёт сохранение и экран не закрывается. */
    val isInputEnabled: Boolean get() = !isSaving && !isCompleted
}
