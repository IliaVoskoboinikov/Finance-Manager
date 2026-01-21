package soft.divan.financemanager.feature.designapp.impl.domain.usecase.impl

import androidx.compose.ui.graphics.Color
import soft.divan.financemanager.feature.designapp.impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.SetCustomAccentColorUseCase
import javax.inject.Inject

class SetCustomAccentColorUseCaseImpl @Inject constructor(
    private val repository: DesignAppRepository
) : SetCustomAccentColorUseCase {
    override suspend fun invoke(color: Color) = repository.setCustomAccentColor(color)
}