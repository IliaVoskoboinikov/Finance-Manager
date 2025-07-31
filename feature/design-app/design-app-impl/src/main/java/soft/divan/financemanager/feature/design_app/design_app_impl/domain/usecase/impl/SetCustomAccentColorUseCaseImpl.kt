package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl

import androidx.compose.ui.graphics.Color
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetCustomAccentColorUseCase
import javax.inject.Inject

class SetCustomAccentColorUseCaseImpl @Inject constructor(
    private val repository: DesignAppRepository
) : SetCustomAccentColorUseCase {
    override suspend fun invoke(color: Color) = repository.setCustomAccentColor(color)
}