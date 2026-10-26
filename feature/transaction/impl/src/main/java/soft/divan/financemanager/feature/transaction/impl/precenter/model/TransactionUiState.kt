package soft.divan.financemanager.feature.transaction.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionUiState {
    data object Loading : TransactionUiState
    data class Success(
        val transaction: TransactionUi,
        val categories: List<CategoryUi>,
        val accounts: List<AccountUi>
    ) : TransactionUiState

    /**
     * Новую операцию не из чего собрать: нет счёта или категории нужного типа.
     * Экран объясняет, чего не хватает, и ждёт данных — как только они появятся в БД
     * (пользователь создал счёт, синк подтянул категории), форма откроется сама.
     */
    data class Empty(val reason: EmptyReason) : TransactionUiState

    data class Error(@field:StringRes val messageRes: Int) : TransactionUiState
}

/** Чего не хватает, чтобы создать операцию. */
enum class EmptyReason {
    /** Нет ни одного активного счёта. */
    NO_ACCOUNTS,

    /** Нет категорий выбранного типа (доход или расход). */
    NO_CATEGORIES
}
