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
    val insights: List<String> = emptyList(),
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

    val recent = expenses.sortedByDescending { it.date }.take(6)

    val insights = buildInsights(categorySpends, totalSpentThisMonth, dailyAverage, daysElapsed, today)

    return DashboardUiState(
        totalSpentThisMonth = totalSpentThisMonth,
        todaySpend = todaySpend,
        dailyAverage = dailyAverage,
        recentExpenses = recent,
        categorySpends = categorySpends,
        insights = insights,
    )
}

private fun buildInsights(
    categorySpends: List<CategorySpend>,
    totalSpentThisMonth: Long,
    dailyAverage: Double,
    daysElapsed: Int,
    today: LocalDate,
): List<String> {
    val insights = mutableListOf<String>()

    categorySpends.firstOrNull()?.let {
        insights.add(
            "Your biggest spend this month is ${it.categoryName} at ${"%.0f".format(it.percent * 100)}% of total expenses."
        )
    }

    val daysInMonth = today.lengthOfMonth()
    val projectedMonthSpend = dailyAverage * daysInMonth
    insights.add("At this rate, you're projected to spend ₹${"%,.0f".format(projectedMonthSpend)} by month end.")

    if (daysElapsed > 0) {
        insights.add("You've spent ₹${"%,.0f".format(totalSpentThisMonth.toDouble() / 100)} across $daysElapsed day${if (daysElapsed > 1) "s" else ""} this month.")
    }

    return insights
}
