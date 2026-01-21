package soft.divan.financemanager.feature.analysis.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.analysis.impl.domain.model.CategoryPieSlice
import soft.divan.financemanager.feature.analysis.impl.domain.usecase.GetCategoryPieChartDataUseCase
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class GetCategoryPieChartDataUseCaseImpl @Inject constructor() : GetCategoryPieChartDataUseCase {
    override fun invoke(
        transactions: List<Transaction>,
        category: List<Category>
    ): List<CategoryPieSlice> {

        if (transactions.isEmpty()) return emptyList()


        val total = transactions.sumOf { it.amount.abs() }

        if (total == BigDecimal.ZERO) return emptyList()

        return transactions
            .groupBy { it.categoryId }
            .map { (categoryId, grouped) ->
                val category = category.find { it.id == grouped.first().categoryId }!!
                val sum = grouped.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount.abs() }
                val percent = sum
                    .multiply(BigDecimal(100))
                    .divide(total, 2, RoundingMode.HALF_UP)
                    .toFloat()

                CategoryPieSlice(
                    categoryId = categoryId,
                    categoryName = category.name,
                    emoji = category.emoji,
                    totalAmount = sum,
                    percent = percent
                )
            }
    }
}