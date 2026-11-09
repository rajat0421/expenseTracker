package com.yourmoney.app.ui.stats

import com.yourmoney.app.model.Expense
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class StatsPeriod(val label: String) { DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly") }

data class StatBucket(
    val label: String,
    val totalPaise: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

private val shortDate = DateTimeFormatter.ofPattern("dd MMM")
private val monthYear = DateTimeFormatter.ofPattern("MMM yyyy")

/** Most-recent-first buckets: last 14 days / last 8 weeks / last 12 months. */
fun buildBuckets(expenses: List<Expense>, period: StatsPeriod, today: LocalDate = LocalDate.now()): List<StatBucket> {
    val byDate = expenses.groupBy { it.date }

    return when (period) {
        StatsPeriod.DAILY -> (0 until 14).map { offset ->
            val day = today.minusDays(offset.toLong())
            val total = byDate[day]?.sumOf { it.amountPaise } ?: 0L
            StatBucket(label = day.format(shortDate), totalPaise = total, startDate = day, endDate = day)
        }

        StatsPeriod.WEEKLY -> (0 until 8).map { offset ->
            val end = today.minusDays((7L * offset))
            val start = end.minusDays(6)
            val total = expenses.sumPaiseInRange(start, end)
            StatBucket(label = "${start.format(shortDate)} - ${end.format(shortDate)}", totalPaise = total, startDate = start, endDate = end)
        }

        StatsPeriod.MONTHLY -> (0 until 12).map { offset ->
            val monthAnchor = today.minusMonths(offset.toLong())
            val start = monthAnchor.withDayOfMonth(1)
            val end = monthAnchor.withDayOfMonth(monthAnchor.lengthOfMonth())
            val total = expenses.sumPaiseInRange(start, end)
            StatBucket(label = monthAnchor.format(monthYear), totalPaise = total, startDate = start, endDate = end)
        }
    }
}

private fun List<Expense>.sumPaiseInRange(start: LocalDate, end: LocalDate): Long =
    filter { !it.date.isBefore(start) && !it.date.isAfter(end) }.sumOf { it.amountPaise }
