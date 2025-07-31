package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetCustomAccentColorUseCase
import javax.inject.Inject

class GetCustomAccentColorUseCaseImpl @Inject constructor(
    private val repository: ThemeRepository
) : GetCustomAccentColorUseCase {
    override fun invoke(): Flow<Color?> = repository.getCustomAccentColor()
}