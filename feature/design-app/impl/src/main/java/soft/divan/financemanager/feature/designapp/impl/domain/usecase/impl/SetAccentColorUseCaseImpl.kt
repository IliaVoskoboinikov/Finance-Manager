package soft.divan.financemanager.feature.designapp.impl.domain.usecase.impl

import soft.divan.financemanager.feature.designapp.impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

class SetAccentColorUseCaseImpl @Inject constructor(
    private val repository: DesignAppRepository
) : SetAccentColorUseCase {
    override suspend fun invoke(color: AccentColor) = repository.setAccentColor(color)
}
