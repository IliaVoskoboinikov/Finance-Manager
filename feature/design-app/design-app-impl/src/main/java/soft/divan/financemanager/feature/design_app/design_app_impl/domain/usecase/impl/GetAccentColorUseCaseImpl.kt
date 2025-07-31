package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

class GetAccentColorUseCaseImpl @Inject constructor(
    private val repository: ThemeRepository
) : GetAccentColorUseCase {
    override fun invoke(): Flow<AccentColor> = repository.getAccentColor()
}