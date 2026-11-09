package com.yourmoney.app.ui.stats

import com.yourmoney.app.model.Expense
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

enum class StatsPeriod(val label: String, val unitLabel: String) {
    DAILY("Daily", "day"),
    WEEKLY("Weekly", "week"),
    MONTHLY("Monthly", "month"),
}

data class StatBucket(
    /** Long label used in the expandable detail row, e.g. "01 Jan - 07 Jan" or "Jan 2026". */
    val label: String,
    /** Short label for the chart's x-axis, e.g. "09" or "Jan". */
    val axisLabel: String,
    val totalPaise: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class StatsSummary(
    val totalPaise: Long,
    val averagePaise: Long,
    val highest: StatBucket?,
    /** Percentage change of the most recent bucket vs the one before it; null if not computable. */
    val trendPercent: Double?,
    val trendIsIncrease: Boolean,
)

private val dayNumber = DateTimeFormatter.ofPattern("dd")
private val shortDate = DateTimeFormatter.ofPattern("dd MMM")
private val monthShort = DateTimeFormatter.ofPattern("MMM")
private val monthYear = DateTimeFormatter.ofPattern("MMM yyyy")

/** Most-recent-first buckets: last 14 days / last 8 weeks / last 12 months. */
fun buildBuckets(expenses: List<Expense>, period: StatsPeriod, today: LocalDate = LocalDate.now()): List<StatBucket> {
    val byDate = expenses.groupBy { it.date }

    return when (period) {
        StatsPeriod.DAILY -> (0 until 14).map { offset ->
            val day = today.minusDays(offset.toLong())
            val total = byDate[day]?.sumOf { it.amountPaise } ?: 0L
            StatBucket(
                label = day.format(shortDate),
                axisLabel = day.format(dayNumber),
                totalPaise = total,
                startDate = day,
                endDate = day,
            )
        }

        StatsPeriod.WEEKLY -> (0 until 8).map { offset ->
            val end = today.minusDays((7L * offset))
            val start = end.minusDays(6)
            val total = expenses.sumPaiseInRange(start, end)
            StatBucket(
                label = "${start.format(shortDate)} - ${end.format(shortDate)}",
                axisLabel = start.format(dayNumber),
                totalPaise = total,
                startDate = start,
                endDate = end,
            )
        }

        StatsPeriod.MONTHLY -> (0 until 12).map { offset ->
            val monthAnchor = today.minusMonths(offset.toLong())
            val start = monthAnchor.withDayOfMonth(1)
            val end = monthAnchor.withDayOfMonth(monthAnchor.lengthOfMonth())
            val total = expenses.sumPaiseInRange(start, end)
            StatBucket(
                label = monthAnchor.format(monthYear),
                axisLabel = monthAnchor.format(monthShort),
                totalPaise = total,
                startDate = start,
                endDate = end,
            )
        }
    }
}

/** [buckets] is most-recent-first, as returned by [buildBuckets]. */
fun buildSummary(buckets: List<StatBucket>): StatsSummary {
    val total = buckets.sumOf { it.totalPaise }
    val average = if (buckets.isEmpty()) 0L else total / buckets.size
    val highest = buckets.maxByOrNull { it.totalPaise }

    val current = buckets.getOrNull(0)?.totalPaise
    val previous = buckets.getOrNull(1)?.totalPaise
    val trendPercent = if (current != null && previous != null && previous > 0) {
        ((current - previous).toDouble() / previous.toDouble()) * 100.0
    } else {
        null
    }

    return StatsSummary(
        totalPaise = total,
        averagePaise = average,
        highest = highest,
        trendPercent = trendPercent?.let { abs(it) },
        trendIsIncrease = (trendPercent ?: 0.0) >= 0,
    )
}

private fun List<Expense>.sumPaiseInRange(start: LocalDate, end: LocalDate): Long =
    filter { !it.date.isBefore(start) && !it.date.isAfter(end) }.sumOf { it.amountPaise }
