package soft.divan.financemanager.feature.design_app.impl.domain.usecase.impl

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.design_app.impl.domain.usecase.GetCustomAccentColorUseCase
import javax.inject.Inject

class GetCustomAccentColorUseCaseImpl @Inject constructor(
    private val repository: DesignAppRepository
) : GetCustomAccentColorUseCase {
    override fun invoke(): Flow<Color?> = repository.getCustomAccentColor()
}