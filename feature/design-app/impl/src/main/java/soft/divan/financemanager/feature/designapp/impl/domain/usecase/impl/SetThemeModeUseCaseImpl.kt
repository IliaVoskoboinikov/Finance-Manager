package soft.divan.financemanager.feature.designapp.impl.domain.usecase.impl

import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.SetThemeModeUseCase
import javax.inject.Inject

class SetThemeModeUseCaseImpl @Inject constructor(
    private val designAppRepository: DesignAppRepository
) : SetThemeModeUseCase {
    override suspend operator fun invoke(mode: ThemeMode) {
        designAppRepository.setThemeMode(mode)
    }
}