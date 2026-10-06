package soft.divan.financemanager.feature.security.impl.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Задан ли PIN — с обновлениями.
 *
 * PIN снимают не только настройки: «забыл PIN», исчерпанные попытки и восстановление после потери
 * ключа стирают его вместе с данными. Замок приложения должен узнать об этом сразу, иначе он
 * требовал бы PIN, которого уже нет.
 */
interface ObservePinSetUseCase {
    operator fun invoke(): Flow<Boolean>
}
