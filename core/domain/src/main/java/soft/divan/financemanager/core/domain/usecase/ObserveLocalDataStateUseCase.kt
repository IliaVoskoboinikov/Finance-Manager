package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.domain.model.LocalDataState

/** Доступны ли локальные данные — по этому состоянию строится главный экран и идёт синк. */
interface ObserveLocalDataStateUseCase {
    operator fun invoke(): StateFlow<LocalDataState>
}
