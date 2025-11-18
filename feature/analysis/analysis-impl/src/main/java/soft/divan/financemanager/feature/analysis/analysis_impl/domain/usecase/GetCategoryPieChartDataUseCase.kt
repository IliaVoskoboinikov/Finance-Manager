package soft.divan.financemanager.feature.analysis.analysis_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.analysis.analysis_impl.domain.model.CategoryPieSlice

interface GetCategoryPieChartDataUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        category: List<Category>
    ): List<CategoryPieSlice>
}