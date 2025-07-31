package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetThemeModeUseCase

import javax.inject.Inject

class GetThemeModeUseCaseImpl @Inject constructor(
    private val designAppRepository: DesignAppRepository
) : GetThemeModeUseCase {
    override operator fun invoke(): Flow<ThemeMode> {
        return designAppRepository.getThemeMode()
    }
}