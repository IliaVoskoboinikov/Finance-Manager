package soft.divan.financemanager.presenter

import soft.divan.financemanager.core.domain.model.LocalDataState

/** Что показывает хост поверх всего приложения — см. [resolveLocalDataScreen]. */
internal enum class LocalDataScreen {
    /** Ключ данных утерян — экран восстановления. */
    KEY_LOST,

    /** База уровня PIN закрыта — замок данных (PIN или отпечаток). */
    DATA_LOCK,

    /** Ключи или PIN ещё не прочитаны — ничего, только фон темы. */
    NOTHING,

    /** Данные открыты, но PIN задан и не введён в этой сессии — замок интерфейса. */
    UI_LOCK,

    /** Главный экран. */
    MAIN
}

/**
 * Выбор экрана хоста. Порядок веток — контракт безопасности: главный экран строится только при
 * открытой базе и пройденном замке, а потеря ключа и запертая база важнее любого «замок пройден».
 *
 * @param pinSet задан ли PIN; `null` — ещё не прочитан.
 * @param unlocked пройден ли замок в этом процессе.
 */
internal fun resolveLocalDataScreen(
    state: LocalDataState,
    pinSet: Boolean?,
    unlocked: Boolean
): LocalDataScreen = when {
    state == LocalDataState.KeyLost -> LocalDataScreen.KEY_LOST
    state is LocalDataState.Locked -> LocalDataScreen.DATA_LOCK
    state == LocalDataState.Initializing || pinSet == null -> LocalDataScreen.NOTHING
    pinSet && !unlocked -> LocalDataScreen.UI_LOCK
    else -> LocalDataScreen.MAIN
}
