package com.yourmoney.app.ui.dashboard

import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.paiseToRupees
import java.time.LocalDate

data class CategorySpend(val categoryId: Long, val categoryName: String, val amountPaise: Long, val percent: Float)

data class DashboardUiState(
    val totalSpentThisMonth: Long = 0,
    val todaySpend: Long = 0,
    val dailyAverage: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val categorySpends: List<CategorySpend> = emptyList(),
    val pendingReviewCount: Int = 0,
)

fun buildDashboardState(expenses: List<Expense>, categoryNames: Map<Long, String>): DashboardUiState {
    val today = LocalDate.now()
    val monthExpenses = expenses.filter { it.date.month == today.month && it.date.year == today.year }

    val totalSpentThisMonth = monthExpenses.sumOf { it.amountPaise }
    val todaySpend = expenses.filter { it.date == today }.sumOf { it.amountPaise }
    val daysElapsed = today.dayOfMonth.coerceAtLeast(1)
    val dailyAverage = totalSpentThisMonth.paiseToRupees() / daysElapsed

    val categoryTotals = monthExpenses
        .groupBy { it.categoryId }
        .mapValues { (_, items) -> items.sumOf { it.amountPaise } }
        .toList()
        .sortedByDescending { it.second }

    val categorySpends = categoryTotals.map { (categoryId, amountPaise) ->
        CategorySpend(
            categoryId = categoryId,
            categoryName = categoryNames[categoryId] ?: "Uncategorized",
            amountPaise = amountPaise,
            percent = if (totalSpentThisMonth > 0) (amountPaise.toFloat() / totalSpentThisMonth) else 0f,
        )
    }

    val recent = expenses.sortedByDescending { it.date }.take(5)

    return DashboardUiState(
        totalSpentThisMonth = totalSpentThisMonth,
        todaySpend = todaySpend,
        dailyAverage = dailyAverage,
        recentExpenses = recent,
        categorySpends = categorySpends,
    )
}
